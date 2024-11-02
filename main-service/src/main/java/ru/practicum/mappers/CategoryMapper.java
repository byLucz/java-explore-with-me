package ru.practicum.mappers;

import ru.practicum.model.category.Category;
import ru.practicum.model.category.dto.CategoryDto;

public class CategoryMapper {
    public static CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName()
        );
    }

    public static Category toCategory(CategoryDto categoryDto) {
        return new Category(
                categoryDto.getId() != null ? categoryDto.getId() : 0,
                categoryDto.getName() != null ? categoryDto.getName() : ""
        );
    }
}
