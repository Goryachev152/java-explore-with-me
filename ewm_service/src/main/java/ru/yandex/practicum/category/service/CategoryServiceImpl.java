package ru.yandex.practicum.category.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.category.dto.CategoryDto;
import ru.yandex.practicum.category.dto.CategoryMapper;
import ru.yandex.practicum.category.dto.NewCategoryDto;
import ru.yandex.practicum.category.model.Category;
import ru.yandex.practicum.category.repository.CategoryRepository;
import ru.yandex.practicum.event.repository.EventRepository;
import ru.yandex.practicum.exception.ConflictException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.exception.ValidationException;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Transactional
    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new ConflictException("Категория с названием " + newCategoryDto.getName() + " присутствует");
        }
        Category category = CategoryMapper.mapToCategory(newCategoryDto);
        CategoryDto categoryDto = CategoryMapper.mapToCategoryDto(categoryRepository.save(category));
        log.info("Категория {} id = {} добавлена в сервис", categoryDto.getName(), categoryDto.getId());
        return categoryDto;
    }

    @Transactional
    @Override
    public CategoryDto updateCategory(NewCategoryDto newCategoryDto, Long catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(() ->
                new NotFoundException("Категория с id = " + catId + "не найдена"));
        if (!newCategoryDto.getName().equals(category.getName())) {
            if (categoryRepository.existsByName(newCategoryDto.getName())) {
                throw new ConflictException("Категория с названием '" + newCategoryDto.getName() + "' уже существует");
            }
        }
        Category updateCategory = new Category(catId, newCategoryDto.getName());
        categoryRepository.save(updateCategory);
        log.info("Категория id = {} обновлена, новое название {}", catId, updateCategory.getName());
        return CategoryMapper.mapToCategoryDto(updateCategory);
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        if (from < 0 || size < 0) {
            throw new ValidationException("Параметры from и size не могут быль отрицательным числом");
        }
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Category> categories = categoryRepository.findAll(pageable);
        return categories.stream().map(CategoryMapper::mapToCategoryDto).toList();
    }

    @Override
    public CategoryDto findByCategoryId(Long catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(() ->
                new NotFoundException("Категория с id = " + catId + "не найдена"));
        return CategoryMapper.mapToCategoryDto(category);
    }

    @Override
    public void deleteCategory(Long catId) {
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Категория с id = " + catId + " не найдена");
        }
        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("Попытка удалить категорию с привязанными событиями");
        }
        categoryRepository.deleteById(catId);
    }
}
