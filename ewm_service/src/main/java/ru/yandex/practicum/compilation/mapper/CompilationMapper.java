package ru.yandex.practicum.compilation.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.yandex.practicum.compilation.dto.CompilationDto;
import ru.yandex.practicum.compilation.dto.NewCompilationDto;
import ru.yandex.practicum.compilation.model.Compilation;
import ru.yandex.practicum.event.dto.EventShortDto;
import ru.yandex.practicum.event.model.Event;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, builder = @Builder(disableBuilder = false))
public interface CompilationMapper {

    @Mapping(target = "events", source = "events")
    CompilationDto toDto(Compilation compilation, List<EventShortDto> events);

    @Mapping(target = "events", source = "events")
    Compilation toCompilation(NewCompilationDto dto, List<Event> events);
}
