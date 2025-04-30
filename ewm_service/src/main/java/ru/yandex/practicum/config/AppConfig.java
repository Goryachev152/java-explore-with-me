package ru.yandex.practicum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.yandex.pacticum.StatsClient;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public StatsClient getStatsClient(RestTemplate restTemplate, @Value("${stats-server.url}") String url) {
        return new StatsClient(restTemplate, url);
    }
}
