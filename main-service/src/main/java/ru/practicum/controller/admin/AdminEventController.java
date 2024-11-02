package ru.practicum.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.event.dto.UpdateEventRequest;
import ru.practicum.model.event.dto.EventFullDto;
import ru.practicum.searchingparams.PresentationParams;
import ru.practicum.service.EventService;
import ru.practicum.searchingparams.AdminParams;
import ru.practicum.enums.EventStates;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminEventController {
    private final EventService eventService;

    @GetMapping("/events")
    public List<EventFullDto> getEventsWithFilteringForAdmin(
            @RequestParam(required = false) List<Integer> users,
            @RequestParam(required = false) List<EventStates> states,
            @RequestParam(required = false) List<Integer> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        AdminParams adminParams = new AdminParams(users, states, categories, rangeStart, rangeEnd);
        PresentationParams presentationParams = new PresentationParams(null, from, size);
        log.info("(A) Получен запрос на вывод списка событий с фильтрацией");
        return eventService.getEventsWithFilteringForAdmin(adminParams, presentationParams);
    }

    @PatchMapping("/events/{eventId}")
    public EventFullDto updateEvent(@PathVariable Integer eventId,
                                    @RequestBody @Valid UpdateEventRequest updateEventRequest) {
        log.info("(A) Получен запрос на обновление события с id = {}", eventId);
        return eventService.updateByAdmin(eventId, updateEventRequest);
    }
}
