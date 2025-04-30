package ru.yandex.practicum.compilation.dto;

import lombok.Builder;
import lombok.Value;
import ru.yandex.practicum.event.dto.EventShortDto;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class CompilationDto {
    Long id;
    List<EventShortDto> events;
    Boolean pinned;
    String title;
}
