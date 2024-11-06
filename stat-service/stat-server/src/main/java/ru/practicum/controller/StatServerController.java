package ru.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.StatServerService;
import ru.practicum.EndpointHit;
import ru.practicum.StatsView;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatServerController {
    private final StatServerService statServ;

    @PostMapping("/hit")
    public void saveNewHit(@Valid @RequestBody EndpointHit hit) {
        log.info("Получен запрос на сохранение нового просмотра '{}'", hit);
        statServ.saveHit(hit);
    }

    @GetMapping("/stats")
    public List<StatsView> getStat(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(name = "uris", required = false) List<String> uris,
            @RequestParam(name = "unique", defaultValue = "false") boolean unique
    ) {
        log.info("Получен запрос на получение статистики: start={}, end={}, uris={}, unique={}", start, end, uris, unique);

        if (start.isAfter(end)) {
            throw new ValidationException("Дата начала не может быть позже даты окончания.");
        }

        return statServ.getStats(start, end, uris, unique);
    }

}