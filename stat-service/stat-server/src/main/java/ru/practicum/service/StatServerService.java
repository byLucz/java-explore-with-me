package ru.practicum.service;

import ru.practicum.EndpointHit;
import ru.practicum.StatsView;

import java.time.LocalDateTime;
import java.util.List;

public interface StatServerService {

    void saveHit(EndpointHit hitDto);

    List<StatsView> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);

}
