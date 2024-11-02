package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.enums.EventStates;
import ru.practicum.model.event.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {

    Page<Event> findAllByInitiatorId(int initiatorId, Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "WHERE e.state = :state " +
            "AND ((LOWER(e.annotation) LIKE CONCAT('%', :text, '%') " +
            "OR LOWER(e.description) LIKE CONCAT('%', :text, '%')) OR :text IS NULL) " +
            "AND (e.category.id IN :category OR :category IS NULL) " +
            "AND (e.paid = :paid OR :paid IS NULL) " +
            "AND (e.eventDate BETWEEN :start AND :end) " +
            "ORDER BY e.eventDate")
    Page<Event> findByParametersForPublic(@Param("state") EventStates state,
                                          @Param("text") String text,
                                          @Param("category") List<Integer> categories,
                                          @Param("paid") Boolean paid,
                                          @Param("start") LocalDateTime rangeStart,
                                          @Param("end") LocalDateTime rangeEnd, Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "WHERE (e.initiator.id IN :users OR :users IS NULL) " +
            "AND (e.state IN :states OR :states IS NULL) " +
            "AND (e.category.id IN :category OR :category IS NULL) " +
            "AND (e.eventDate BETWEEN :start AND :end)")
    Page<Event> findByParametersForAdmin(@Param("users") List<Integer> users,
                                         @Param("states") List<EventStates> states,
                                         @Param("category") List<Integer> categories,
                                         @Param("start") LocalDateTime rangeStart,
                                         @Param("end") LocalDateTime rangeEnd, Pageable pageable);
}
