package ru.practicum.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatClient;
import ru.practicum.EndpointHit;
import ru.practicum.StatsView;
import ru.practicum.enums.AdminStates;
import ru.practicum.enums.EventStates;
import ru.practicum.enums.SortTypes;
import ru.practicum.enums.UserStates;
import ru.practicum.exception.BadRequestEWMException;
import ru.practicum.exception.UnreachableEWMException;
import ru.practicum.model.Constants;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.Location;
import ru.practicum.model.event.dto.*;
import ru.practicum.model.user.User;
import ru.practicum.mappers.EventMapper;
import ru.practicum.mappers.LocationMapper;
import ru.practicum.searchingparams.PresentationParams;
import ru.practicum.model.category.Category;
import ru.practicum.repository.EventRepository;
import ru.practicum.searchingparams.AdminParams;
import ru.practicum.searchingparams.UserParams;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final StatClient statClient;
    private final LocationService locationService;
    private final CategoryService categoryService;
    private final UserService userService;

    @Transactional
    public EventFullDto save(int userId, NewEventDto newEventDto) {
        validateEventDate(newEventDto.getEventDate(), Constants.DATE_TIME_FORMAT, 2);
        User initiator = userService.getUser(userId);
        Category category = categoryService.getCategory(newEventDto.getCategory());
        Location location = locationService.saveLocation(LocationMapper.toLocation(newEventDto.getLocation()));
        return EventMapper.toEventFullDto(eventRepository.save(EventMapper.toEvent(newEventDto, initiator, category, location)));
    }

    @Transactional(readOnly = true)
    public List<Event> getEventsByIds(List<Integer> eventIds) {
        return eventRepository.findAllById(eventIds);
    }

    @Transactional
    public void save(Event event) {
        eventRepository.save(event);
    }

    @Transactional
    public EventFullDto updateByAdmin(int eventId, UpdateEventRequest updateEventRequest) {
        Event updatingEvent = getEvent(eventId);
        if (updateEventRequest.getStateAction() != null) {
            handleAdminStateAction(updatingEvent, updateEventRequest.getStateAction());
        }
        Category category = getCategory(updateEventRequest.getCategory());
        return updateEvent(updatingEvent, updateEventRequest, category, Constants.UPDATE_TIME_LIMIT_ADMIN);
    }

    @Transactional
    public EventFullDto updateByUser(int userId, int eventId, UpdateEventRequest updateEventRequest) {
        Event updatingEvent = getEvent(eventId);
        validateUserPermissions(updatingEvent, userId);
        Category category = getCategory(updateEventRequest.getCategory());
        if (updateEventRequest.getStateAction() != null) {
            handleUserStateAction(updatingEvent, updateEventRequest.getStateAction());
        }
        return updateEvent(updatingEvent, updateEventRequest, category, Constants.UPDATE_TIME_LIMIT_USER);
    }

    private void handleAdminStateAction(Event event, String action) {
        AdminStates adminStates = AdminStates.from(action).orElseThrow(() -> new IllegalArgumentException("Unknown state: " + action));
        if (adminStates.equals(AdminStates.PUBLISH_EVENT)) {
            validateEventState(event, EventStates.PENDING, "Можно публиковать только события в ожидании публикации");
            event.setState(EventStates.PUBLISHED);
        } else {
            validateEventState(event, EventStates.PENDING, "Можно отклонить только события в ожидании публикации");
            event.setState(EventStates.CANCELED);
        }
    }

    private void handleUserStateAction(Event event, String action) {
        UserStates userStates = UserStates.from(action).orElseThrow(() -> new IllegalArgumentException("Unknown state: " + action));
        event.setState(userStates == UserStates.SEND_TO_REVIEW ? EventStates.PENDING : EventStates.CANCELED);
    }

    private void validateUserPermissions(Event event, int userId) {
        if (event.getInitiator().getId() != userId) {
            throw new BadRequestEWMException("Событие может редактировать только инициатор");
        }
        if (event.getState().equals(EventStates.PUBLISHED)) {
            throw new UnreachableEWMException("Нельзя изменить опубликованное событие");
        }
    }

    private EventFullDto updateEvent(Event event, UpdateEventRequest request, Category category, int timeLimit) {
        Optional.ofNullable(request.getAnnotation()).ifPresent(event::setAnnotation);
        Optional.ofNullable(category).ifPresent(event::setCategory);
        Optional.ofNullable(request.getDescription()).ifPresent(event::setDescription);
        Optional.ofNullable(request.getEventDate()).ifPresent(date -> {
            validateEventDate(date, Constants.DATE_TIME_FORMAT, timeLimit);
            event.setEventDate(LocalDateTime.parse(date, Constants.DATE_TIME_FORMAT));
        });
        Optional.ofNullable(request.getLocation()).ifPresent(location -> updateLocation(event, location));
        Optional.ofNullable(request.getPaid()).ifPresent(event::setPaid);
        Optional.ofNullable(request.getParticipantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(request.getRequestModeration()).ifPresent(event::setRequestModeration);
        Optional.ofNullable(request.getTitle()).ifPresent(event::setTitle);
        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    private void updateLocation(Event event, LocationDto locationDto) {
        Location locationForUpdate = LocationMapper.toLocation(locationDto);
        locationForUpdate.setId(event.getLocation().getId());
        locationService.saveLocation(locationForUpdate);
        event.setLocation(locationForUpdate);
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> getUserEvents(int userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<EventFullDto> fullEventsDtoNoViews = eventRepository.findallbyinitiatorId(userId, pageable)
                .stream().map(EventMapper::toEventFullDto).collect(Collectors.toList());
        return addStatsAndConvertToShortDto(fullEventsDtoNoViews);
    }

    private List<EventShortDto> addStatsAndConvertToShortDto(List<EventFullDto> fullEvents) {
        return addStatsToEventFullDtoInformation(fullEvents)
                .stream().map(EventMapper::fromFullToShortEventDTO)
                .collect(Collectors.toList());
    }

    private List<EventFullDto> addStatsToEventFullDtoInformation(List<EventFullDto> eventsFullDto) {
        List<String> uris = eventsFullDto.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());
        Map<Integer, Integer> statistic = getHitsStatistic(uris);
        eventsFullDto.forEach(event -> event.setViews(statistic.getOrDefault(event.getId(), 0)));
        return eventsFullDto;
    }

    private Map<Integer, Integer> getHitsStatistic(List<String> uris) {
        LocalDateTime start = LocalDateTime.now().minusYears(Constants.FREE_TIME_INTERVAL);
        LocalDateTime end = LocalDateTime.now().plusYears(Constants.FREE_TIME_INTERVAL);
        List<StatsView> stats = statClient.getStat(start, end, uris, true);
        Map<Integer, Integer> statistic = new HashMap<>();
        for (StatsView statsDtoOut : stats) {
            String idStr = statsDtoOut.getUri().replace("/events/", "");
            statistic.put(Integer.parseInt(idStr), Math.toIntExact(statsDtoOut.getHits()));
        }
        return statistic;
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsWithFilteringForPublic(UserParams searchParams,
                                                               PresentationParams presentationParams,
                                                               HttpServletRequest servletRequest) {
        log.info("Вызов клиента статистики");
        statClient.saveStat(new EndpointHit(
                "emw-main-service",
                "/events",
                servletRequest.getRemoteAddr(),
                LocalDateTime.now().format(Constants.DATE_TIME_FORMAT)));
        Pageable pageable = PageRequest.of(presentationParams.getFrom() / presentationParams.getSize(), presentationParams.getSize());
        Page<Event> events = eventRepository.findByParametersForPublic(
                EventStates.PUBLISHED,
                Optional.ofNullable(searchParams.getText()).map(String::toLowerCase).orElse(null),
                searchParams.getCategories(),
                searchParams.getPaid(),
                resolveDateRangeStart(searchParams.getRangeStart()),
                resolveDateRangeEnd(searchParams.getRangeEnd()),
                pageable
        );
        return filterAndSortEvents(events, presentationParams.getSort());
    }

    private LocalDateTime resolveDateRangeStart(LocalDateTime rangeStart) {
        return rangeStart != null ? rangeStart : LocalDateTime.now();
    }

    private LocalDateTime resolveDateRangeEnd(LocalDateTime rangeEnd) {
        return rangeEnd != null ? rangeEnd : LocalDateTime.now().plusYears(Constants.FREE_TIME_INTERVAL);
    }

    private List<EventShortDto> filterAndSortEvents(Page<Event> events, SortTypes sortTypes) {
        List<EventFullDto> fullEventsDtoNoViews = events
                .stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());
        List<EventFullDto> filteredByViews = addStatsToEventFullDtoInformation(fullEventsDtoNoViews);
        List<EventFullDto> filteredByLimit = filteredByViews.stream()
                .filter(event -> event.getParticipantLimit() == 0 || event.getConfirmedRequests() < event.getParticipantLimit())
                .collect(Collectors.toList());

        if (sortTypes == SortTypes.VIEWS) {
            filteredByLimit.sort(Comparator.comparing(EventFullDto::getViews).reversed());
        }
        return filteredByLimit.stream()
                .map(EventMapper::fromFullToShortEventDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventFullDto getEventForPublic(int id, HttpServletRequest servletRequest) {
        Event event = getEvent(id);
        if (event.getState() != EventStates.PUBLISHED) {
            throw new NoSuchElementException("Событие с id " + id + " не опубликовано");
        }
        statClient.saveStat(new EndpointHit(
                "emw-main-service",
                "/events/" + id,
                servletRequest.getRemoteAddr(),
                LocalDateTime.now().format(Constants.DATE_TIME_FORMAT)));
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        Map<Integer, Integer> statistic = getHitsStatistic(Collections.singletonList("/events/" + id));
        eventFullDto.setViews(statistic.getOrDefault(id, 0));
        return eventFullDto;
    }

    @Transactional(readOnly = true)
    public EventFullDto getEventOfUserForPrivate(int userId, int eventId) {
        User user = userService.getUser(userId);
        Event event = getEvent(eventId);
        if (event.getInitiator().getId() != userId) {
            throw new UnreachableEWMException("Нельзя просматривать чужие события");
        }
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        Map<Integer, Integer> statistic = getHitsStatistic(Collections.singletonList("/events/" + eventId));
        eventFullDto.setViews(statistic.getOrDefault(eventId, 0));
        return eventFullDto;
    }

    @Transactional(readOnly = true)
    public List<EventFullDto> getEventsWithFilteringForAdmin(AdminParams searchParams,
                                                             PresentationParams presentationParams) {
        Pageable pageable = PageRequest.of(presentationParams.getFrom() / presentationParams.getSize(), presentationParams.getSize());
        Page<Event> events = eventRepository.findByParametersForAdmin(
                searchParams.getUsers(),
                searchParams.getStates(),
                searchParams.getCategories(),
                resolveDateRangeStart(searchParams.getRangeStart()),
                resolveDateRangeEnd(searchParams.getRangeEnd()),
                pageable
        );
        List<EventFullDto> fullEventsDtoNoViews = events.stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());
        return addStatsToEventFullDtoInformation(fullEventsDtoNoViews);
    }

    @Transactional(readOnly = true)
    public Event getEvent(int eventId) {
        return eventRepository.getReferenceById(eventId);
    }

    private Category getCategory(Integer categoryId) {
        return categoryId != null ? categoryService.getCategory(categoryId) : null;
    }

    private void validateEventDate(String date, java.time.format.DateTimeFormatter formatter, int hoursOffset) {
        if (LocalDateTime.parse(date, formatter).isBefore(LocalDateTime.now().plusHours(hoursOffset))) {
            throw new BadRequestEWMException("Дата события не может быть раньше, чем через " + hoursOffset + " ч. от текущего момента");
        }
    }

    private void validateEventState(Event event, EventStates expectedState, String errorMessage) {
        if (!event.getState().equals(expectedState)) {
            throw new UnreachableEWMException(errorMessage);
        }
    }
}
