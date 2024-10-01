package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.model.Hits;
import ru.practicum.repository.StatServerRepo;
import ru.practicum.EndpointHit;
import ru.practicum.StatsView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatServerServiceImpl implements StatServerService {
    private final StatServerRepo repository;
    private final String pattern = "yyyy-MM-dd HH:mm:ss";

    @Override
    @Transactional
    public void saveHit(EndpointHit hitDto) {
        LocalDateTime dateTime = LocalDateTime.parse(hitDto.getTimestamp(), DateTimeFormatter.ofPattern(pattern));
        repository.save(new Hits(null, hitDto.getApp(), hitDto.getUri(), hitDto.getIp(), dateTime));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatsView> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (uris != null && !uris.isEmpty()) {
            return unique ? repository.getDistinctByUris(uris.toArray(new String[0]), start, end)
                    .stream()
                    .map(this::convertToStatsView)
                    .collect(Collectors.toList())
                    : repository.getByUris(uris.toArray(new String[0]), start, end)
                    .stream()
                    .map(this::convertToStatsView)
                    .collect(Collectors.toList());
        } else {
            return unique ? repository.getDistinctByStartAndEnd(start, end)
                    .stream()
                    .map(this::convertToStatsView)
                    .collect(Collectors.toList())
                    : repository.getByStartAndEnd(start, end)
                    .stream()
                    .map(this::convertToStatsView)
                    .collect(Collectors.toList());
        }
    }

    private StatsView convertToStatsView(StatsView hitOutcomeDto) {
        StatsView statsView = new StatsView();
        statsView.setApp(hitOutcomeDto.getApp());
        statsView.setUri(hitOutcomeDto.getUri());
        statsView.setHits(hitOutcomeDto.getHits());
        return statsView;
    }
}
