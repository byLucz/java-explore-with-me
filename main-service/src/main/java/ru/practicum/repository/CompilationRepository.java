package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.compilation.Compilation;

public interface CompilationRepository extends JpaRepository<Compilation, Integer> {

    @Query("SELECT c FROM Compilation c " +
            "WHERE (CAST(c.pinned AS boolean) = :pinned OR :pinned IS NULL)")
    Page<Compilation> findByPinnedForPublic(@Param("pinned") Boolean pinned, Pageable pageable);
}