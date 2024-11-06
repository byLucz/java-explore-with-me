package ru.practicum.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.types.IntegrityViolationException;
import ru.practicum.exception.types.NotFoundException;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public Category addCategory(Category category) {
        log.info("Начало процесса создания категории");
        categoryRepository.findCategoriesByNameContainingIgnoreCase(category.getName().toLowerCase()).ifPresent(c -> {
            throw new IntegrityViolationException("Категория с именем " + category.getName() + " уже существует");
        });
        Category createdCategory = categoryRepository.save(category);
        log.info("Категория создана");
        return createdCategory;
    }

    @Override
    @Transactional
    public void deleteCategory(long catId) {
        log.info("Начало процесса удаления категории");
        categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException("Категория с ID " + catId + " не существует"));
        if (!eventRepository.findAllByCategoryId(catId).isEmpty()) {
            throw new IntegrityViolationException("Категория с ID " + catId + " уже используется");
        }
        categoryRepository.deleteById(catId);
        log.info("Категория удалена");
    }

    @Override
    @Transactional
    public Category updateCategory(long catId, Category newCategory) {
        log.info("Начало процесса обновления категории");
        Category existingCategory = categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException("Категория с ID=" + catId + " не существует"));
        categoryRepository.findCategoriesByNameContainingIgnoreCase(
                newCategory.getName().toLowerCase()).ifPresent(c -> {
            if (c.getId() != catId) {
                throw new IntegrityViolationException("Категория с именем " + newCategory.getName() + " уже существует");
            }
        });
        existingCategory.setName(newCategory.getName());
        log.info("Категория обновлена");
        return existingCategory;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategories(int from, int size) {
        log.info("Начало процесса получения всех категорий");
        PageRequest pageRequest = PageRequest.of(from, size);
        Page<Category> pageCategories = categoryRepository.findAll(pageRequest);
        List<Category> categories = pageCategories.hasContent() ? pageCategories.getContent() : Collections.emptyList();
        log.info("Категории найдены");
        return categories;
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategory(long catId) {
        log.info("Начало процесса поиска категории");
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException("Категория с ID=" + catId + " не существует"));
        log.info("Категория найдена");
        return category;
    }
}
