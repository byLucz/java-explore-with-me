package ru.practicum;

import java.time.LocalDateTime;
import java.util.List;

public interface StatServerService {

    void saveHit(EndpointHit hitDto);

    List<StatsView> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);

}
