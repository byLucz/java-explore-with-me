package ru.practicum.category;

import ru.practicum.category.model.Category;

import java.util.List;

public interface CategoryService {
    Category addCategory(Category category);

    void deleteCategory(long catId);

    Category updateCategory(long catId, Category category);

    List<Category> getAllCategories(int from, int size);

    Category getCategory(long catId);
}
