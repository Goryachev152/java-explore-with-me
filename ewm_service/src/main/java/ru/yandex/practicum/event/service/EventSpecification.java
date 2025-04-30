package ru.yandex.practicum.event.service;

import org.springframework.data.jpa.domain.Specification;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.model.State;

import java.time.LocalDateTime;
import java.util.List;

public class EventSpecification {
    public static Specification<Event> textInAnnotationOrDescription(String text) {
        return (root, query, cb) -> {
            if (text == null || text.isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + text.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("annotation")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Event> categoryIn(List<Integer> categoryIds) {
        return (root, query, cb) -> {
            if (categoryIds == null || categoryIds.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("category").get("id").in(categoryIds);
        };
    }

    public static Specification<Event> eventDateAfter(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("eventDate"), date);
        };
    }

    public static Specification<Event> eventDateBefore(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("eventDate"), date);
        };
    }

    public static Specification<Event> isAvailable(boolean onlyAvailable) {
        return (root, query, cb) -> {
            if (!onlyAvailable) {
                return cb.conjunction();
            }
            return cb.lessThan(root.get("confirmedRequests"), root.get("participantLimit"));
        };
    }

    public static Specification<Event> onlyPublished() {
        return (root, query, cb) ->
                cb.equal(root.get("state"), State.PUBLISHED);
    }

    public static Specification<Event> sortBySortType(String sort) {
        return (root, query, cb) -> {
            if ("EVENT_DATE".equals(sort)) {
                query.orderBy(cb.asc(root.get("eventDate")));
            } else if ("VIEWS".equals(sort)) {
                query.orderBy(cb.desc(root.get("views")));
            }
            return cb.conjunction();
        };
    }

    public static Specification<Event> hasInitiators(List<Integer> userIds) {
        return (root, query, cb) -> {
            if (userIds == null || userIds.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("initiator").get("id").in(userIds);
        };
    }

    public static Specification<Event> hasStates(List<State> states) {
        return (root, query, cb) -> {
            if (states == null || states.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("state").in(states);
        };
    }
}
