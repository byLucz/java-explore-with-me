package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.model.Hits;
import ru.practicum.StatsView;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatServerRepo extends JpaRepository<Hits, Long> {
    @Query("select new ru.practicum.StatsView(e.app, e.uri, count(e.ip)) from Hits e " +
            "where e.uri in :uris and e.timestamp between :start and :end " +
            "group by e.app, e.uri order by count(e.ip) desc")
    List<StatsView> getByUris(@Param("uris") String[] uris,
                              @Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end);

    @Query("select new ru.practicum.StatsView(e.app, e.uri, count(e.ip)) from Hits e " +
            "where e.timestamp between :start and :end " +
            "group by e.app, e.uri order by count(e.ip) desc")
    List<StatsView> getByStartAndEnd(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    @Query("select new ru.practicum.StatsView(e.app, e.uri, count(distinct e.ip)) from Hits e " +
            "where e.timestamp between :start and :end " +
            "group by e.app, e.uri order by count(distinct e.ip) desc")
    List<StatsView> getDistinctByStartAndEnd(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    @Query("select new ru.practicum.StatsView(e.app, e.uri, count(distinct e.ip)) from Hits e " +
            "where e.uri in :uris and e.timestamp between :start and :end " +
            "group by e.app, e.uri order by count(distinct e.ip) desc")
    List<StatsView> getDistinctByUris(@Param("uris") String[] uris,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);
}