package ru.yandex.practicum.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.model.State;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    boolean existsByCategoryId(Long categoryId);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);

    Page<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    Page<Event> findAll(Specification<Event> spec, Pageable pageable);

    Optional<Event> findByIdAndState(Long eventId, State state);
}
