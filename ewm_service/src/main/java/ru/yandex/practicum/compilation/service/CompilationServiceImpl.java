package ru.yandex.practicum.compilation.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.pacticum.StatsClient;
import ru.yandex.practicum.ResultStatsDto;
import ru.yandex.practicum.compilation.dto.CompilationDto;
import ru.yandex.practicum.compilation.dto.NewCompilationDto;
import ru.yandex.practicum.compilation.dto.UpdateCompilationRequest;
import ru.yandex.practicum.compilation.mapper.CompilationMapper;
import ru.yandex.practicum.compilation.model.Compilation;
import ru.yandex.practicum.compilation.repository.CompilationRepository;
import ru.yandex.practicum.event.dto.EventShortDto;
import ru.yandex.practicum.event.mapper.EventMapper;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.repository.EventRepository;
import ru.yandex.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final StatsClient statsClient;
    private final CompilationMapper compilationMapper;

    @Transactional
    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        List<Event> events = Collections.emptyList();
        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            events = eventRepository.findAllById(newCompilationDto.getEvents());
        }
        Compilation compilation = compilationMapper.toCompilation(newCompilationDto, events);
        compilation = compilationRepository.save(compilation);
        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .toList();
        Map<String, Long> viewsMap = getStats(events, uris, true);
        List<EventShortDto> eventShortDtoList = events.stream()
                .map(event -> eventMapper.toShortDto(event, viewsMap.getOrDefault("/events/" + event.getId(), 0L)))
                .toList();
        return compilationMapper.toDto(compilation, eventShortDtoList);
    }

    @Transactional
    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id= " + compId + " не найдена"));
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getEvents() != null) {
            List<Event> events = eventRepository.findAllById(updateCompilationRequest.getEvents());
            compilation.setEvents(events);
        }
        compilation = compilationRepository.save(compilation);
        List<String> uris = compilation.getEvents().stream()
                .map(event -> "/events/" + event.getId())
                .toList();
        Map<String, Long> viewsMap = getStats(compilation.getEvents(), uris, true);
        List<EventShortDto> eventShortDtoList = compilation.getEvents().stream()
                .map(event -> eventMapper.toShortDto(event, viewsMap.getOrDefault("/events/" + event.getId(), 0L)))
                .toList();
        return compilationMapper.toDto(compilation, eventShortDtoList);
    }

    @Override
    public void deleteCompilation(Long compId) {
        compilationRepository.deleteById(compId);
    }

    @Override
    public List<CompilationDto> findCompilations(Boolean pinned, Integer from, Integer size) {
        int page = size > 0 ? from / size : 0;
        Pageable pageable = PageRequest.of(page, size);
        Page<Compilation> compilationsPage = compilationRepository.findAllByPinned(pinned, pageable);
        List<Compilation> compilations = compilationsPage.getContent();
        List<Event> allEvents = compilations.stream()
                .flatMap(compilation -> compilation.getEvents().stream())
                .collect(Collectors.toList());
        List<String> uris = allEvents.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());
        Map<String, Long> viewsMap = getStats(allEvents, uris, true);
        return compilations.stream()
                .map(compilation -> {
                    List<EventShortDto> eventDtos = compilation.getEvents().stream()
                            .map(event -> {
                                String eventUri = "/events/" + event.getId();
                                Long views = viewsMap.getOrDefault(eventUri, 0L);
                                return eventMapper.toShortDto(event, views);
                            })
                            .collect(Collectors.toList());
                    return compilationMapper.toDto(compilation, eventDtos);
                })
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto findCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException("Подборка " + compId + " не найдена"));
        List<String> uris = compilation.getEvents().stream()
                .map(event -> "/events/" + event.getId())
                .toList();
        Map<String, Long> viewsMap = getStats(compilation.getEvents(), uris, true);
        List<EventShortDto> eventShortDtoList = compilation.getEvents().stream()
                .map(event -> eventMapper.toShortDto(event, viewsMap.getOrDefault("/events/" + event.getId(), 0L)))
                .toList();
        return compilationMapper.toDto(compilation, eventShortDtoList);
    }

    private Map<String, Long> getStats(List<Event> events, List<String> uris, Boolean unique) {
        List<ResultStatsDto> stats;
        LocalDateTime start = events.stream()
                .map(Event::getPublishedOn)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElseGet(() -> LocalDateTime.now().minusWeeks(1));
        stats = statsClient.getStats(
                start,
                LocalDateTime.now(),
                uris,
                unique
        );
        return stats.stream()
                .collect(Collectors.toMap(
                        ResultStatsDto::getUri,
                        ResultStatsDto::getHits
                ));
    }
}

