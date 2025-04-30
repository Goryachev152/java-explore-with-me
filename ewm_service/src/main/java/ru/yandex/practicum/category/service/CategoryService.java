package ru.yandex.practicum.category.service;

import ru.yandex.practicum.category.dto.CategoryDto;
import ru.yandex.practicum.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    CategoryDto updateCategory(NewCategoryDto newCategoryDto, Long catId);

    List<CategoryDto> getCategories(Integer from, Integer size);

    CategoryDto findByCategoryId(Long catId);

    void deleteCategory(Long catId);
}
