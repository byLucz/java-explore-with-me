package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String formattedStart = start.truncatedTo(ChronoUnit.SECONDS).format(formatter);
        String formattedEnd = end.truncatedTo(ChronoUnit.SECONDS).format(formatter);

        ResponseEntity<List<StatsView>> response = rest.exchange(
                statServerUrl + "/stats?start={start}&end={end}&uris={uris}&unique={unique}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<StatsView>>() {},
                formattedStart, formattedEnd, urisToSend, unique
        );

        return response.getBody();
    }


    public void saveHit(String appName, HttpServletRequest request) {
        EndpointHit hit = new EndpointHit();
        hit.setApp(appName);
        hit.setUri(request.getRequestURI());
        hit.setIp(request.getRemoteAddr());
        hit.setTimestamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        WebClient.builder()
                .baseUrl(statServerUrl)
                .build()
                .post()
                .uri("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(hit), EndpointHit.class)
                .retrieve()
                .onStatus(
                        status -> status.isError(),
                        response -> response.bodyToMono(String.class).map(errorMessage -> new RuntimeException("Ошибка на сервере: " + errorMessage))
                )
                .bodyToMono(String.class)
                .block();
    }

}
