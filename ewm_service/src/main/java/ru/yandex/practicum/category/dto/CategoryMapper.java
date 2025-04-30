package ru.yandex.practicum.category.dto;

import ru.yandex.practicum.category.model.Category;

public class CategoryMapper {

    public static Category mapToCategory(NewCategoryDto newCategoryDto) {
        return Category.builder()
                .name(newCategoryDto.getName())
                .build();
    }

    public static CategoryDto mapToCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
