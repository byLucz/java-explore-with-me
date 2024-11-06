package ru.practicum.requests;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.types.IntegrityViolationException;
import ru.practicum.exception.types.NotFoundException;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.requests.dto.mapper.RequestMapper;
import ru.practicum.requests.model.Request;
import ru.practicum.requests.model.Status;
import ru.practicum.requests.repository.RequestsRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final RequestsRepository requestsRepository;
    private final RequestMapper requestMapper;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getAllRequests(long userId) {
        log.info("Начало процесса поиска всех заявок пользователя с ID {}", userId);
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException(
                "Пользователь с ID = " + userId + " не найден"));
        List<Request> requests = requestsRepository.findAllByRequesterId(userId);
        log.info("Все заявки пользователя найдены");
        return requestMapper.listRequestToListParticipationRequestDto(requests);
    }

    @Override
    @Transactional
    public ParticipationRequestDto addRequest(long userId, long eventId) {
        log.info("Начало процесса создания заявки для пользователя с ID {} и события с ID {}", userId, eventId);
        requestsRepository.findByEventIdAndRequesterId(eventId, userId).ifPresent(
                r -> {
                    throw new IntegrityViolationException(
                            "Заявка с userId " + userId + " и eventId " + eventId + " уже существует");
                });

        eventRepository.findByIdAndInitiatorId(eventId, userId).ifPresent(
                r -> {
                    throw new IntegrityViolationException(
                            "Пользователь с ID " + userId + " является инициатором события с ID " + eventId);
                });

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(
                "Пользователь с ID = " + userId + " не найден"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(
                "Событие с ID = " + eventId + " не найдено"));

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new IntegrityViolationException("Событие с ID = " + eventId + " не опубликовано");
        }

        List<Request> confirmedRequests = requestsRepository.findAllByStatusAndEventId(Status.CONFIRMED, eventId);

        if ((!event.getParticipantLimit().equals(0L)) && (event.getParticipantLimit() == confirmedRequests.size())) {
            throw new IntegrityViolationException("Лимит заявок превышен");
        }

        Request request = new Request();
        request.setCreated(LocalDateTime.now());
        request.setRequester(user);
        request.setEvent(event);

        if ((event.getParticipantLimit().equals(0L)) || (!event.getRequestModeration())) {
            request.setStatus(Status.CONFIRMED);
        } else {
            request.setStatus(Status.PENDING);
        }

        request = requestsRepository.save(request);
        log.info("Заявка была создана");
        return requestMapper.requestToParticipationRequestDto(request);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(long userId, long requestId) {
        log.info("Начало процесса отмены заявки с ID {} для пользователя с ID {}", requestId, userId);
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException(
                "Пользователь с ID = " + userId + " не найден"));
        Request request = requestsRepository.findById(requestId).orElseThrow(() -> new NotFoundException(
                "Заявка с ID = " + requestId + " не найдена"
        ));
        request.setStatus(Status.CANCELED);
        log.info("Заявка была отменена");
        return requestMapper.requestToParticipationRequestDto(request);
    }
}
