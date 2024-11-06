package ru.practicum.event;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.practicum.StatClient;
import ru.practicum.StatsView;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.dto.mapper.EventMapper;
import ru.practicum.event.enums.EventTypes;
import ru.practicum.event.enums.AdminStates;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.types.DateTimeException;
import ru.practicum.exception.types.NotFoundException;
import ru.practicum.exception.types.RestrictionsViolationException;
import ru.practicum.requests.dto.EventRequestStatusUpdateRequestDto;
import ru.practicum.requests.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.requests.dto.mapper.RequestMapper;
import ru.practicum.requests.model.Request;
import ru.practicum.requests.model.Status;
import ru.practicum.requests.repository.RequestsRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import static ru.practicum.event.model.QEvent.event;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestsRepository requestsRepository;
    private final StatClient statClient;
    private final EventMapper eventMapper;
    private final RequestMapper requestMapper;

    @Transactional
    @Override
    public EventFullDto addEvent(NewEventDto newEventDto, long userId) {
        log.info("Начало процесса создания события");
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с id=" + newEventDto.getCategory() + " не найдена"));

        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new DateTimeException("Дата и время, на которые запланировано событие, не могут быть " +
                    "раньше чем через два часа от текущего момента");
        }

        if (newEventDto.getPaid() == null) {
            newEventDto.setPaid(false);
        }
        if (newEventDto.getRequestModeration() == null) {
            newEventDto.setRequestModeration(true);
        }
        if (newEventDto.getParticipantLimit() == null) {
            newEventDto.setParticipantLimit(0L);
        }

        Event newEvent = eventMapper.newEventDtoToEvent(newEventDto);
        newEvent.setCategory(category);
        newEvent.setCreatedOn(LocalDateTime.now());
        newEvent.setInitiator(initiator);
        newEvent.setPublishedOn(LocalDateTime.now());
        newEvent.setState(State.PENDING);
        newEvent.setConfirmedRequests(0L);

        Event event = eventRepository.save(newEvent);
        EventFullDto eventFullDto = eventMapper.eventToEventFullDto(event);
        eventFullDto.setViews(0L);

        log.info("Событие создано");
        return eventFullDto;
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto findEventById(long userId, long eventId) {
        log.info("Начало процесса поиска события");

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        List<StatsView> viewStats = getViewStats(List.of(event));
        EventFullDto eventFullDto = eventMapper.eventToEventFullDto(event);

        if (!CollectionUtils.isEmpty(viewStats)) {
            eventFullDto.setViews(viewStats.get(0).getHits());
        } else {
            eventFullDto.setViews(0L);
        }

        log.info("Событие найдено");
        return eventFullDto;
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> findEventsByUser(long userId, int from, int size) {
        log.info("Начало процесса поиска событий");

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        PageRequest pageRequest = PageRequest.of(from, size);
        BooleanExpression byUserId = event.initiator.id.eq(userId);
        Page<Event> pageEvents = eventRepository.findAll(byUserId, pageRequest);
        List<Event> events = pageEvents.getContent();
        setViews(events);

        List<EventShortDto> eventsShortDto = eventMapper.listEventToListEventShortDto(events);

        log.info("События найдены");
        return eventsShortDto;
    }

    @Transactional
    @Override
    public EventFullDto updateEvent(UpdateEventUserRequest updateEvent, long userId, long eventId) {
        log.info("Начало процесса обновления события");

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        if (event.getState().equals(State.PUBLISHED)) {
            throw new RestrictionsViolationException("Можно изменить только отмененные события или события, " +
                    "ожидающие модерацию");
        }

        if (updateEvent.getAnnotation() != null && !updateEvent.getAnnotation().isBlank()) {
            event.setAnnotation(updateEvent.getAnnotation());
        }
        if (updateEvent.getCategory() != null) {
            Category category = categoryRepository.findById(updateEvent.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id=" + updateEvent.getCategory() + " не найдена"));
            event.setCategory(category);
        }
        if (updateEvent.getDescription() != null && !updateEvent.getDescription().isBlank()) {
            event.setDescription(updateEvent.getDescription());
        }
        if (updateEvent.getEventDate() != null) {
            if (updateEvent.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new DateTimeException("Дата и время, на которые запланировано событие, не могут быть " +
                        "раньше чем через два часа от текущего момента");
            } else {
                event.setEventDate(updateEvent.getEventDate());
            }
        }
        if (updateEvent.getLocation() != null) {
            event.setLocation(updateEvent.getLocation());
        }
        if (updateEvent.getPaid() != null) {
            event.setPaid(updateEvent.getPaid());
        }
        if (updateEvent.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEvent.getParticipantLimit());
        }
        if (updateEvent.getRequestModeration() != null) {
            event.setRequestModeration(updateEvent.getRequestModeration());
        }
        if (updateEvent.getTitle() != null && !updateEvent.getTitle().isBlank()) {
            event.setTitle(updateEvent.getTitle());
        }
        if (updateEvent.getStateAction() != null) {
            switch (updateEvent.getStateAction()) {
                case CANCEL_REVIEW -> event.setState(State.CANCELED);
                case SEND_TO_REVIEW -> event.setState(State.PENDING);
            }
        }

        log.info("Событие обновлено");
        return eventMapper.eventToEventFullDto(event);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> findRequestByEventId(long userId, long eventId) {
        log.info("Начало процесса поиска заявок");

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }

        List<Request> requests = requestsRepository.findByEventId(eventId);

        log.info("Заявки найдены");
        return requestMapper.listRequestToListParticipationRequestDto(requests);
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResultDto updateRequestByEventId(EventRequestStatusUpdateRequestDto updateRequests,
                                                                    long userId,
                                                                    long eventId) {
        log.info("Начало процесса обновления заявок");

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
        List<Request> confirmedRequests = requestsRepository.findAllByStatusAndEventId(Status.CONFIRMED, eventId);

        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() == confirmedRequests.size()) {
            throw new RestrictionsViolationException("Лимит заявок на это событие достигнут, " +
                    "осталось " + (event.getParticipantLimit() - event.getConfirmedRequests()) + " свободных мест");
        }

        List<Request> requests = requestsRepository.findByIdIn(updateRequests.getRequestIds());

        if (requests.stream().map(Request::getStatus).anyMatch(status -> !status.equals(Status.PENDING))) {
            throw new RestrictionsViolationException("Статус можно изменить только для заявок, находящихся " +
                    "в состоянии ОЖИДАНИЕ");
        }

        requests.forEach(request -> request.setStatus(updateRequests.getStatus()));

        if (updateRequests.getStatus().equals(Status.CONFIRMED)) {
            event.setConfirmedRequests(event.getConfirmedRequests() + updateRequests.getRequestIds().size());
        }

        log.info("Заявки обновлены");
        return requestMapper.toEventRequestStatusResult(null, requests);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getAllPublicEvents(String text, List<Long> categories, Boolean paid,
                                                  LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                  boolean onlyAvailable, EventTypes sort, int from, int size) {
        log.info("Начало процесса поиска событий по публичному доступу");

        if ((rangeStart != null) && (rangeEnd != null) && (rangeStart.isAfter(rangeEnd))) {
            throw new DateTimeException("Время начала позже времени окончания");
        }
        Page<Event> events;
        PageRequest pageRequest = getCustomPage(from, size, sort);
        BooleanBuilder builder = new BooleanBuilder();

        if (text != null) {
            builder.and(event.annotation.containsIgnoreCase(text.toLowerCase())
                    .or(event.description.containsIgnoreCase(text.toLowerCase())));
        }

        if (!CollectionUtils.isEmpty(categories)) {
            builder.and(event.category.id.in(categories));
        }

        if (rangeStart != null && rangeEnd != null) {
            builder.and(event.eventDate.between(rangeStart, rangeEnd));
        } else if (rangeStart == null && rangeEnd != null) {
            builder.and(event.eventDate.between(LocalDateTime.MIN, rangeEnd));
        } else if (rangeStart != null) {
            builder.and(event.eventDate.between(rangeStart, LocalDateTime.MAX));
        }

        if (onlyAvailable) {
            builder.and(event.participantLimit.eq(0L))
                    .or(event.participantLimit.gt(event.confirmedRequests));
        }

        if (builder.getValue() != null) {
            events = eventRepository.findAll(builder.getValue(), pageRequest);
        } else {
            events = eventRepository.findAll(pageRequest);
        }

        setViews(events.getContent());
        log.info("События найдены по публичному доступу");
        return eventMapper.listEventToListEventShortDto(events.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getPublicEventById(long id) {
        log.info("Начало процесса поиска события по публичному доступу");
        Event event = eventRepository.findByIdAndState(id, State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + id + " не найдено"));
        setViews(List.of(event));
        log.info("Событие найдено по публичному доступу");
        return eventMapper.eventToEventFullDto(event);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventFullDto> getAllAdminEvents(List<Long> users, State state, List<Long> categories,
                                                LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        log.info("Начало процесса поиска событий для администратора");
        Page<Event> pageEvents;
        PageRequest pageRequest = getCustomPage(from, size, null);
        BooleanBuilder builder = new BooleanBuilder();

        if (!CollectionUtils.isEmpty(users) && !users.contains(0L)) {
            builder.and(event.initiator.id.in(users));
        }

        if (state != null) {
            builder.and(event.state.eq(state));
        }

        if (!CollectionUtils.isEmpty(categories) && !categories.contains(0L)) {
            builder.and(event.category.id.in(categories));
        }

        if (rangeStart != null && rangeEnd != null) {
            if (rangeStart.isAfter(rangeEnd)) {
                throw new DateTimeException("Время начала позже времени окончания");
            }
            builder.and(event.eventDate.between(rangeStart, rangeEnd));
        } else if (rangeStart == null && rangeEnd != null) {
            builder.and(event.eventDate.between(LocalDateTime.MIN, rangeEnd));
        } else if (rangeStart != null) {
            builder.and(event.eventDate.between(rangeStart, LocalDateTime.MAX));
        }

        if (builder.getValue() != null) {
            pageEvents = eventRepository.findAll(builder.getValue(), pageRequest);
        } else {
            pageEvents = eventRepository.findAll(pageRequest);
        }

        List<Event> events = pageEvents.getContent();
        setViews(events);
        log.info("События найдены для администратора");
        return eventMapper.listEventToListEventFullDto(events);
    }

    @Transactional
    @Override
    public EventFullDto updateEventAdmin(UpdateEventAdminRequest updateEvent, long eventId) {
        log.info("Начало процесса обновления события администратором");

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        if (updateEvent.getAnnotation() != null && !updateEvent.getAnnotation().isBlank()) {
            event.setAnnotation(updateEvent.getAnnotation());
        }
        if (updateEvent.getCategory() != null) {
            Category category = categoryRepository.findById(updateEvent.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id=" + updateEvent.getCategory() + " не найдена"));
            event.setCategory(category);
        }
        if (updateEvent.getDescription() != null && !updateEvent.getDescription().isBlank()) {
            event.setDescription(updateEvent.getDescription());
        }
        if (updateEvent.getEventDate() != null) {
            if (updateEvent.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new DateTimeException("Дата и время, на которые запланировано событие, не могут быть " +
                        "раньше чем через два часа от текущего момента");
            } else {
                event.setEventDate(updateEvent.getEventDate());
            }
        }
        if (updateEvent.getLocation() != null) {
            event.setLocation(updateEvent.getLocation());
        }
        if (updateEvent.getPaid() != null) {
            event.setPaid(updateEvent.getPaid());
        }
        if (updateEvent.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEvent.getParticipantLimit());
        }
        if (updateEvent.getRequestModeration() != null) {
            event.setRequestModeration(updateEvent.getRequestModeration());
        }
        if (updateEvent.getTitle() != null && !updateEvent.getTitle().isBlank()) {
            event.setTitle(updateEvent.getTitle());
        }
        if (updateEvent.getStateAction() != null) {
            setStateByAdmin(event, updateEvent.getStateAction());
        }

        log.info("Событие обновлено администратором");
        return eventMapper.eventToEventFullDto(event);
    }

    private void setStateByAdmin(Event event, AdminStates adminStates) {
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1)) &&
                adminStates.equals(AdminStates.PUBLISH_EVENT)) {
            throw new DateTimeException("Дата начала изменяемого события должна быть не ранее чем через " +
                    "час от даты публикации.");
        }

        if (adminStates.equals(AdminStates.PUBLISH_EVENT)) {
            if (!event.getState().equals(State.PENDING)) {
                throw new RestrictionsViolationException("Событие может быть опубликовано только в статусе " +
                        "ожидания публикации");
            }
            event.setState(State.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        } else {
            if (event.getState().equals(State.PUBLISHED)) {
                throw new RestrictionsViolationException("Событие может быть отклонено, только если оно еще " +
                        "не опубликовано");
            }
            event.setState(State.CANCELED);
        }
    }

    private PageRequest getCustomPage(int from, int size, EventTypes sort) {
        if (sort != null) {
            return switch (sort) {
                case EVENT_DATE -> PageRequest.of(from, size, Sort.by(Sort.Direction.ASC, "eventDate"));
                case VIEWS -> PageRequest.of(from, size, Sort.by(Sort.Direction.ASC, "views"));
            };
        } else {
            return PageRequest.of(from, size);
        }

    }

    private List<StatsView> getViewStats(List<Event> events) {
        List<String> url = events.stream()
                .map(event -> "/events/" + event.getId())
                .toList();
        Optional<List<StatsView>> statsView = Optional.ofNullable(statClient
                .getStat(LocalDateTime.now().minusYears(20), LocalDateTime.now(), url, true)
        );
        return statsView.orElse(Collections.emptyList());
    }

    private void setViews(List<Event> events) {
        if (CollectionUtils.isEmpty(events)) {
            return;
        }
        Map<String, Long> mapUriAndHits = getViewStats(events).stream()
                .collect(Collectors.toMap(StatsView::getUri, StatsView::getHits));

        for (Event event : events) {
            event.setViews(mapUriAndHits.getOrDefault("/events/" + event.getId(), 0L));
        }
    }
}
