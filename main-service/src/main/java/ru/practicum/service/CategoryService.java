package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.BadRequestEWMException;
import ru.practicum.exception.UnreachableEWMException;
import ru.practicum.mappers.CategoryMapper;
import ru.practicum.model.category.Category;
import ru.practicum.model.category.dto.CategoryDto;
import ru.practicum.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryDto save(CategoryDto categoryDto) {
        if ((categoryDto.getName() == null) || (categoryDto.getName().isBlank())) {
            throw new BadRequestEWMException("Переданы некорректные данные для создания категории");
        }
        if (!categoryRepository.findCategoriesByName(categoryDto.getName()).isEmpty()) {
            throw new UnreachableEWMException("Категория с таким именем уже существует");
        }
        return CategoryMapper.toCategoryDto(categoryRepository.save(CategoryMapper.toCategory(categoryDto)));
    }

    @Transactional
    public CategoryDto update(int catId, CategoryDto categoryDto) {
        Category updatingCategory = getCategory(catId);
        if ((categoryDto.getName() == null) || (categoryDto.getName().isBlank())) {
            throw new BadRequestEWMException("Переданы некорректные данные для создания категории");
        }
        if (updatingCategory.getName().equals(categoryDto.getName())) {
            categoryDto.setId(updatingCategory.getId());
            return categoryDto;
        } else if (!categoryRepository.findCategoriesByName(categoryDto.getName()).isEmpty()) {
            throw new UnreachableEWMException("Категория с таким именем уже существует");
        }
        updatingCategory.setName(categoryDto.getName());
        return CategoryMapper.toCategoryDto(categoryRepository.save(updatingCategory));
    }

    @Transactional(readOnly = true)
    public Category getCategory(int categoryId) {
        return categoryRepository.getReferenceById(categoryId);
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryDtoOut(int categoryId) {

        return CategoryMapper.toCategoryDto(categoryRepository.getReferenceById(categoryId));
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable).stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCategory(int catId) {
        Category category = getCategory(catId);
        category.getId();
        categoryRepository.delete(category);
    }
}
