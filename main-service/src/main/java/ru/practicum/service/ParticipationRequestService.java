package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.enums.EventStates;
import ru.practicum.enums.RequestStates;
import ru.practicum.exception.BadRequestEWMException;
import ru.practicum.exception.UnreachableEWMException;
import ru.practicum.mappers.ParticipationRequestMapper;
import ru.practicum.model.event.Event;
import ru.practicum.model.request.ParticipationRequest;
import ru.practicum.model.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.model.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.model.request.dto.ParticipationRequestDto;
import ru.practicum.model.user.User;
import ru.practicum.repository.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipationRequestService {
    private final ParticipationRequestRepository participationRequestRepository;
    private final EventService eventService;
    private final UserService userService;

    @Transactional
    public ParticipationRequestDto save(int requesterId, int eventId) {
        User requester = userService.getUser(requesterId);
        Event event = eventService.getEvent(eventId);
        if (requesterId == event.getInitiator().getId()) {
            throw new UnreachableEWMException("Организатор не может делать запрос на участие в своём событии");
        }
        ParticipationRequest participationRequest = getRequestByEventAndRequester(eventId, requesterId);
        if (participationRequest != null) {
            throw new UnreachableEWMException("Повторный запрос невозможен");
        }
        if (!event.getState().equals(EventStates.PUBLISHED)) {
            throw new UnreachableEWMException("Событие не опубликовано");
        }
        if (event.getParticipantLimit() != 0 && event.getConfirmedRequests() == event.getParticipantLimit()) {
            throw new UnreachableEWMException("Достигнут предел количества участников");
        }
        RequestStates requestStates = RequestStates.PENDING;
        if (!event.getCheckinRequests() || event.getParticipantLimit() == 0) {
            requestStates = RequestStates.CONFIRMED;
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventService.save(event);
        }

        ParticipationRequest participationRequestToSave = new ParticipationRequest(
                0,
                LocalDateTime.now(),
                event, requester,
                requestStates
        );
        log.info("Сохраняется запрос с параметрами: eventId={}, requesterId={}, status={}", event.getId(), requester.getId(), requestStates);
        return ParticipationRequestMapper.toParticipationRequestDto(participationRequestRepository.save(participationRequestToSave));
    }

    @Transactional
    public void saveAll(List<ParticipationRequest> updatedParticipationRequests) {
        participationRequestRepository.saveAll(updatedParticipationRequests);
    }

    @Transactional
    public ParticipationRequestDto cancel(int userId, int requestId) {
        ParticipationRequest canceledParticipationRequest = participationRequestRepository.getReferenceById(requestId);
        if (canceledParticipationRequest.getRequester().getId() != userId) {
            throw new BadRequestEWMException("Отказано в доступе!");
        }
        canceledParticipationRequest.setStatus(RequestStates.CANCELED);
        return ParticipationRequestMapper.toParticipationRequestDto(participationRequestRepository.save(canceledParticipationRequest));
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(int userId) {
        userService.getUser(userId).getId();
        return participationRequestRepository.findAllByRequester_Id(userId).stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsForParticipationInUserEvent(int userId, int eventId) {
        int initiatorId = eventService.getEvent(eventId).getInitiator().getId();
        if (userId != initiatorId) {
            throw new BadRequestEWMException("События пользователя не найдено");
        }
        return participationRequestRepository.findAllByEvent_Id(eventId).stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequest> getRequestByIds(List<Integer> requestIds) {
        return participationRequestRepository.findAllByIdIn(requestIds);
    }

    @Transactional(readOnly = true)
    public ParticipationRequest getRequestByEventAndRequester(int eventId, int requesterId) {
        return participationRequestRepository.findOneByEvent_IdAndRequester_Id(eventId, requesterId);
    }

    @Transactional
    public EventRequestStatusUpdateResult updateRequestsStatus(int userId, int eventId,
                                                               EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        userService.getUser(userId).getId();
        Event event = eventService.getEvent(eventId);
        List<Integer> requestIds = eventRequestStatusUpdateRequest.getRequestIds();
        List<ParticipationRequest> participationRequests = getRequestByIds(requestIds);
        if (event.getParticipantLimit() == 0 || !event.getCheckinRequests()) {
            event.setConfirmedRequests(event.getConfirmedRequests() + participationRequests.size());
            eventService.save(event);
            List<ParticipationRequestDto> participationRequestsDto = participationRequests.stream()
                    .map(ParticipationRequestMapper::toParticipationRequestDto)
                    .collect(Collectors.toList());
            return new EventRequestStatusUpdateResult(participationRequestsDto, new ArrayList<>());
        }
        if (event.getConfirmedRequests() == event.getParticipantLimit()) {
            throw new UnreachableEWMException("Допустимый предел количества участников. Заявки отклонены");
        }
        List<ParticipationRequest> updatedParticipationRequests = new ArrayList<>();
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
        for (ParticipationRequest participationRequest : participationRequests) {
            if (!participationRequest.getStatus().equals(RequestStates.PENDING)) {
                throw new UnreachableEWMException("Статус можно изменить только в режиме ожидания. Заявки отклонены");
            }
            if (event.getParticipantLimit() > event.getConfirmedRequests() &&
                    RequestStates.from(eventRequestStatusUpdateRequest.getStatus()).orElseThrow(() ->
                                    new IllegalArgumentException("Unknown state: " + eventRequestStatusUpdateRequest.getStatus()))
                            .equals(RequestStates.CONFIRMED)) {
                participationRequest.setStatus(RequestStates.CONFIRMED);
                updatedParticipationRequests.add(participationRequest);
                confirmedRequests.add(ParticipationRequestMapper.toParticipationRequestDto(participationRequest));
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            } else {
                participationRequest.setStatus(RequestStates.REJECTED);
                updatedParticipationRequests.add(participationRequest);
                rejectedRequests.add(ParticipationRequestMapper.toParticipationRequestDto(participationRequest));
            }
        }
        eventService.save(event);
        saveAll(updatedParticipationRequests);
        return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }
}
