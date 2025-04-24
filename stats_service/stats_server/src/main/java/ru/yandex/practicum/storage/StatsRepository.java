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

    @Query("select new ru.yandex.practicum.ResultStatsDto(s.app, s.uri, count(s.id)) " +
            "from Viewing as s " +
            "where s.timestamp between :start and :end " +
            "group by s.app, s.uri " +
            "order by count(s.id) desc")
    List<ResultStatsDto> findAllUrisFalseUnique(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select new ru.yandex.practicum.ResultStatsDto(s.app, s.uri, count(distinct s.ip)) " +
            "from Viewing as s " +
            "where s.timestamp between :start and :end " +
            "group by s.app, s.uri " +
            "order by count(distinct s.ip) desc")
    List<ResultStatsDto> findAllUrisTrueUnique(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select new ru.yandex.practicum.ResultStatsDto(s.app, s.uri, count(s.id)) " +
            "from Viewing as s " +
            "where s.timestamp between :start and :end and s.uri in :uris " +
            "group by s.app, s.uri " +
            "order by count(distinct s.id) desc")
    List<ResultStatsDto> findByListUrisFalseUnique(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                                   @Param("uris") List<String> uris);

    @Query("select new ru.yandex.practicum.ResultStatsDto(s.app, s.uri, count(distinct s.ip)) " +
            "from Viewing as s " +
            "where s.timestamp between :start and :end and s.uri in :uris " +
            "group by s.app, s.uri " +
            "order by count(distinct s.ip) desc")
    List<ResultStatsDto> findByListUrisTrueUnique(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                                  @Param("uris") List<String> uris);
}
