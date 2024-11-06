package ru.practicum.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.dto.UpdateCategoryDto;
import ru.practicum.category.dto.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.CategoryService;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryAdminController {
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        log.info("Добавление категории: {}", newCategoryDto);
        Category category = categoryMapper.newCategoryDtoToCategory(newCategoryDto);
        return categoryMapper.categoryToCategoryDto(categoryService.addCategory(category));
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable long catId) {
        log.info("Удаление категории: {}", catId);
        categoryService.deleteCategory(catId);
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(@PathVariable long catId, @RequestBody @Valid UpdateCategoryDto dto) {
        log.info("Обновление категории: {}", dto);
        return categoryMapper.categoryToCategoryDto(
                categoryService.updateCategory(catId, categoryMapper.updateCategoryDtoToCategory(dto)));
    }
}
