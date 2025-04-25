package ru.yandex.practicum;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ResultStatsDto {
    String app;
    String uri;
    Long hits;
}
