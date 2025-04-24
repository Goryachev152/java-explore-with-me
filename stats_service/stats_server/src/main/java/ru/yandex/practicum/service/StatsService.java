package ru.yandex.practicum.service;

import ru.yandex.practicum.ResultStatsDto;
import ru.yandex.practicum.ViewingRequestDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    void addHit(ViewingRequestDto viewingRequestDto);

    List<ResultStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
