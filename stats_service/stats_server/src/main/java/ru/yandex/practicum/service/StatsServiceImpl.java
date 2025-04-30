package ru.yandex.practicum.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.ResultStatsDto;
import ru.yandex.practicum.ViewingRequestDto;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.model.Viewing;
import ru.yandex.practicum.model.ViewingMapper;
import ru.yandex.practicum.storage.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Override
    @Transactional
    public void addHit(ViewingRequestDto viewingRequestDto) {
        Viewing viewing = ViewingMapper.mapToViewing(viewingRequestDto);
        statsRepository.save(viewing);
        log.info("Просмотр {} добавлен в сервис статистики", viewing);
    }

    @Override
    public List<ResultStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start.isAfter(end)) {
            throw new ValidationException("Дата начала не может быть позже даты окончания");
        }
        List<ResultStatsDto> resultList;
        if (Objects.isNull(uris) || uris.isEmpty()) {
            if (!unique) {
                resultList = statsRepository.findAllUrisFalseUnique(start, end);
            } else {
                resultList = statsRepository.findAllUrisTrueUnique(start, end);
            }
        } else {
            if (!unique) {
                resultList = statsRepository.findByListUrisFalseUnique(start, end, uris);
            } else {
                resultList = statsRepository.findByListUrisTrueUnique(start, end, uris);
            }
        }
        return resultList;
    }
}
