package ru.yandex.practicum.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder(toBuilder = true)
public class UpdateCompilationRequest {
    private List<Long> events;
    private Boolean pinned = false;
    @Size(min = 2, max = 50)
    private String title;
}
