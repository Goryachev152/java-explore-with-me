package ru.yandex.pacticum;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.yandex.practicum.ResultStatsDto;
import ru.yandex.practicum.ViewingRequestDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StatsClient {
    final RestTemplate restTemplate;
    final String serverUrl;

    public StatsClient(RestTemplate restTemplate, @Value("${stats-server.url}") String serverUrl) {
        this.restTemplate = restTemplate;
        this.serverUrl = serverUrl;
    }

    public void addHit(ViewingRequestDto viewingRequestDto) {
        String uri = UriComponentsBuilder.fromHttpUrl(serverUrl)
                .path("/hit")
                .toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ViewingRequestDto> entity = new HttpEntity<>(viewingRequestDto, headers);
        restTemplate.exchange(uri, HttpMethod.POST, entity, Void.class);
    }

    public List<ResultStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                         List<String> uris, Boolean unique) {
        String uri = UriComponentsBuilder.fromHttpUrl(serverUrl)
                .path("/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("uris", uris)
                .queryParam("unique", unique)
                .toUriString();
        return restTemplate.exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<List<ResultStatsDto>>() {
        }).getBody();
    }
}
