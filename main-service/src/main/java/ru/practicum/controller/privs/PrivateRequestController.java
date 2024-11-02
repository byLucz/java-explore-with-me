package ru.practicum.controller.privs;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.request.dto.ParticipationRequestDto;
import ru.practicum.service.ParticipationRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}")
@RequiredArgsConstructor
@Slf4j
public class PrivateRequestController {
    private final ParticipationRequestService participationRequestService;

    @GetMapping("/requests")
    public List<ParticipationRequestDto> getUserRequests(@PathVariable @Positive Integer userId) {
        log.info("(P) Получен запрос на получение реквестов пользователя с id={} на участие в других событиях ", userId);
        return participationRequestService.getUserRequests(userId);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto save(@PathVariable @Positive Integer userId,
                                        @RequestParam("eventId") @Positive Integer eventId) {
        log.info("(P) Получен запрос на создание реквеста для события с eventId = {} от пользователя с id={}", eventId, userId);
        return participationRequestService.save(userId, eventId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public ParticipationRequestDto cancel(@PathVariable @Positive Integer userId,
                                          @PathVariable @Positive Integer requestId) {
        log.info("(P) Получен запрос на отмену реквеста для события с eventId = {} от пользователя с id = {}", requestId, userId);
        return participationRequestService.cancel(userId, requestId);
    }
}
