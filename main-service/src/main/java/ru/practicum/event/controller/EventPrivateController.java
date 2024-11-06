package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.StatClient;
import ru.practicum.config.AppConfig;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.EventService;
import ru.practicum.requests.dto.EventRequestStatusUpdateRequestDto;
import ru.practicum.requests.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.requests.dto.ParticipationRequestDto;

import java.util.List;

@RestController
@RequestMapping("users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
@Validated
public class EventPrivateController {
    private final EventService eventService;
    private final StatClient statClient;
    private final AppConfig appConfig;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@RequestBody @Valid NewEventDto newEventDto, @PathVariable long userId) {
        log.info("Получен POST запрос на добавление события {} от пользователя с userId = {}", newEventDto, userId);
        return eventService.addEvent(newEventDto, userId);
    }

    @GetMapping("/{eventId}")
    public EventFullDto findEventById(@PathVariable long userId,
                                      @PathVariable long eventId,
                                      HttpServletRequest request) throws InterruptedException {
        log.info("Получен GET запрос на поиск события по id {} от пользователя с userId = {}", eventId, userId);
        EventFullDto event = eventService.findEventById(userId, eventId);
        statClient.saveHit(appConfig.getAppName(), request);
        return event;
    }

    @GetMapping
    public List<EventShortDto> findEventsByUser(@PathVariable long userId,
                                                @RequestParam(defaultValue = "0") int from,
                                                @RequestParam(defaultValue = "10") int size,
                                                HttpServletRequest request) {
        log.info("Получен GET запрос на поиск событий по userId = {} от = {} размер = {}", userId, from, size);
        List<EventShortDto> events = eventService.findEventsByUser(userId, from, size);
        statClient.saveHit(appConfig.getAppName(), request);
        return events;
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@RequestBody @Valid UpdateEventUserRequest updateEventUserRequest,
                                    @PathVariable long userId,
                                    @PathVariable long eventId) {
        log.info("Получен PATCH запрос на обновление события с eventId = {} от пользователя с userId = {}, тело запроса {}",
                eventId, userId, updateEventUserRequest);
        return eventService.updateEvent(updateEventUserRequest, userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> findRequestByEventId(@PathVariable long userId, @PathVariable long eventId) {
        log.info("Получен GET запрос на поиск запроса по id события = {} от пользователя с userId = {}",
                eventId, userId);
        return eventService.findRequestByEventId(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResultDto updateRequestByEventId(@RequestBody
                                                                    @Valid
                                                                    EventRequestStatusUpdateRequestDto updateRequests,
                                                                    @PathVariable long userId,
                                                                    @PathVariable long eventId) {
        log.info("Получен PATCH запрос на обновление запроса с eventId = {} от пользователя с userId = {}, тело запроса {}",
                eventId, userId, updateRequests);
        return eventService.updateRequestByEventId(updateRequests, userId, eventId);
    }
}
