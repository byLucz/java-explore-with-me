package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.category.Category;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findCategoriesByName(String name);
}