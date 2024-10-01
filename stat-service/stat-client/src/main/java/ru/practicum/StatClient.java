package ru.practicum;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Service
public class StatClient {
    private final RestTemplate rest;
    @Value("${stat-client.url}")
    private String statServerUrl;

    public void addHit(EndpointHit hitDto) {
        rest.postForLocation(statServerUrl + "/hit", hitDto);
    }

    public List<StatsView> getStat(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        String urisToSend = uris != null ? String.join(",", uris) : "";

        ResponseEntity<List<StatsView>> response = rest.exchange(
                statServerUrl + "/stats?start={start}&end={end}&uris={uris}&unique={unique}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<StatsView>>() {},
                start, end, urisToSend, unique);

        return response.getBody();
    }
}