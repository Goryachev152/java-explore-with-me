package ru.yandex.practicum.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Value;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
public class CategoryDto {
    Long id;
    String name;
}
