package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatClient {
    @Value("${stat-client.url}")
    private String statServerUrl;
    private final RestTemplate rest;

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

    public void saveStat(EndpointHit hit) {
        String resp = WebClient.builder()
                .baseUrl(statServerUrl)
                .build()
                .post()
                .uri("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(hit), EndpointHit.class)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}