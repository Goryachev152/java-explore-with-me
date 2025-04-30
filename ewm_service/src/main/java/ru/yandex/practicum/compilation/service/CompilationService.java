package ru.yandex.practicum.compilation.service;

import ru.yandex.practicum.compilation.dto.CompilationDto;
import ru.yandex.practicum.compilation.dto.NewCompilationDto;
import ru.yandex.practicum.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto createCompilation(NewCompilationDto dto);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest);

    void deleteCompilation(Long compId);

    List<CompilationDto> findCompilations(Boolean pinned, Integer from, Integer size);

    CompilationDto findCompilationById(Long compId);
}
