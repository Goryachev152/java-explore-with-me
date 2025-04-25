package ru.yandex.practicum.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.ResultStatsDto;
import ru.yandex.practicum.model.Viewing;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<Viewing, Long> {

    @Query("select new ru.yandex.practicum.ResultStatsDto(v.app, v.uri, count(v.id)) " +
            "from Viewing as v " +
            "where v.viewingData between :start and :end " +
            "group by v.app, v.uri " +
            "order by count(v.id) desc")
    List<ResultStatsDto> findAllUrisFalseUnique(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select new ru.yandex.practicum.ResultStatsDto(v.app, v.uri, count(distinct v.ip)) " +
            "from Viewing as v " +
            "where v.viewingData between :start and :end " +
            "group by v.app, v.uri " +
            "order by count(distinct v.ip) desc")
    List<ResultStatsDto> findAllUrisTrueUnique(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select new ru.yandex.practicum.ResultStatsDto(v.app, v.uri, count(v.id)) " +
            "from Viewing as v " +
            "where v.viewingData between :start and :end and v.uri in :uris " +
            "group by v.app, v.uri " +
            "order by count(distinct v.id) desc")
    List<ResultStatsDto> findByListUrisFalseUnique(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                                   @Param("uris") List<String> uris);

    @Query("select new ru.yandex.practicum.ResultStatsDto(v.app, v.uri, count(distinct v.ip)) " +
            "from Viewing as v " +
            "where v.viewingData between :start and :end and v.uri in :uris " +
            "group by v.app, v.uri " +
            "order by count(distinct v.ip) desc")
    List<ResultStatsDto> findByListUrisTrueUnique(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                                  @Param("uris") List<String> uris);
}
