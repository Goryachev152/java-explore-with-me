package ru.yandex.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.yandex.pacticum.StatsClient;
import ru.yandex.practicum.ResultStatsDto;
import ru.yandex.practicum.ViewingRequestDto;
import ru.yandex.practicum.category.model.Category;
import ru.yandex.practicum.category.repository.CategoryRepository;
import ru.yandex.practicum.event.dto.EventFullDto;
import ru.yandex.practicum.event.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.event.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.event.dto.EventShortDto;
import ru.yandex.practicum.event.dto.NewEventDto;
import ru.yandex.practicum.event.dto.UpdateEventAdminRequest;
import ru.yandex.practicum.event.dto.UpdateEventUserRequest;
import ru.yandex.practicum.event.mapper.EventMapper;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.model.Location;
import ru.yandex.practicum.event.model.State;
import ru.yandex.practicum.event.model.StateAction;
import ru.yandex.practicum.event.repository.EventRepository;
import ru.yandex.practicum.event.repository.LocationRepository;
import ru.yandex.practicum.exception.ConflictException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.request.dto.ParticipationRequestDto;
import ru.yandex.practicum.request.mapper.RequestMapper;
import ru.yandex.practicum.request.model.Request;
import ru.yandex.practicum.request.model.RequestStatus;
import ru.yandex.practicum.request.repository.RequestRepository;
import ru.yandex.practicum.user.model.User;
import ru.yandex.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final EventMapper eventMapper;
    private final StatsClient statsClient;

    @Transactional
    @Override
    public EventFullDto createEvent(NewEventDto newEventDto, Long userId) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Дата и время на которые намечено событие не может быть раньше, " +
                    "чем через два часа от текущего момента");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с id = " + newEventDto.getCategory() + " не найдена"));
        Location location = locationRepository.save(newEventDto.getLocation());
        Event event = eventMapper.newEventToEvent(newEventDto, category, location);
        event.setConfirmedRequests(0L);
        event.setCreatedOn(LocalDateTime.now());
        event.setInitiator(user);
        event.setState(State.PENDING);
        Event saveEvent = eventRepository.save(event);
        log.info("Событие с id = {} добавлено в сервис", saveEvent.getId());
        return eventMapper.toFullDto(event, 0L);
    }

    @Transactional
    @Override
    public EventFullDto updateEventPrivate(UpdateEventUserRequest updateEventUserRequest, Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + eventId + " не найдено"));
        if (event.getState() != State.CANCELED && event.getState() != State.PENDING) {
            throw new ConflictException("Обновление возможно только для отмененных или ожидающих модерации событий");
        }
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь " + userId + " не существует");
        }
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь " + userId + " не существует");
        }
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ValidationException("Пользователь с id =" + userId + " не является создателем события с id = " + eventId);
        }
        if (updateEventUserRequest.getEventDate() != null) {
            LocalDateTime newEventDate = updateEventUserRequest.getEventDate();
            if (newEventDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException("Время события указано раньше, чем через два часа от текущего момента");
            }
            event.setEventDate(newEventDate);
        }
        if (updateEventUserRequest.getStateAction() != null) {
            switch (updateEventUserRequest.getStateAction()) {
                case CANCEL_REVIEW -> event.setState(State.CANCELED);
                case REJECT_EVENT -> event.setState(State.REJECT);
                case SEND_TO_REVIEW -> event.setState(State.PENDING);
                case PUBLISH_EVENT -> event.setState(State.PUBLISHED);
            }
        }
        updateIfNotNull(updateEventUserRequest.getAnnotation(), event::setAnnotation);
        updateIfNotNull(updateEventUserRequest.getDescription(), event::setDescription);
        updateIfNotNull(updateEventUserRequest.getTitle(), event::setTitle);
        if (updateEventUserRequest.getLocation() != null) {
            event.setLocation(locationRepository.save(updateEventUserRequest.getLocation()));
        }
        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toFullDto(updatedEvent, 0L);
    }

    @Override
    public EventFullDto findByUserIdAndEventId(Long userId, Long eventId) {
        Optional<Event> event = eventRepository.findByIdAndInitiatorId(eventId, userId);
        if (event.isEmpty()) {
            throw new NotFoundException("Событие с id = " + eventId + " не найдено");
        }
        List<String> uris = List.of("/events/" + eventId);
        Long views = getViews(event.get().getCreatedOn(), LocalDateTime.now(), uris, true);
        return eventMapper.toFullDto(event.get(), views);
    }

    @Override
    public List<EventShortDto> findEventByUserId(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events;
        events = eventRepository.findAllByInitiatorId(userId, pageable).getContent();
        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .toList();
        Map<String, Long> viewsMap = getStats(events, uris, true);
        return events.stream()
                .map(event -> eventMapper.toShortDto(event, viewsMap.getOrDefault("/events/" + event.getId(), 0L)))
                .toList();
    }

    @Override
    public List<ParticipationRequestDto> findRequestEventPrivate(Long userId, Long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие с id = " + eventId + " не найдено"));
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ValidationException("Пользователь с id = " + userId + " не является создателем события с id = " + eventId);
        }
        List<Request> requests = requestRepository.findAllByEventId(eventId);
        return requests.stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateRequestEventPrivate(EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest, Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + eventId + " не найдено"));
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ConflictException("Подтверждение заявок не требуется");
        }
        List<Request> requests = requestRepository.findAllById(eventRequestStatusUpdateRequest.getRequestIds());
        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequests = new ArrayList<>();

        if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConflictException("Лимит участников достигнут");
        }
        requests.forEach(request -> {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Статус заявки не в состоянии ожидания");
            }
            if (event.getConfirmedRequests() < event.getParticipantLimit() && eventRequestStatusUpdateRequest.getStatus() == RequestStatus.CONFIRMED) {
                request.setStatus(RequestStatus.CONFIRMED);
                confirmedRequests.add(request);
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            } else {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(request);
            }
        });
        eventRepository.save(event);
        requestRepository.saveAll(requests);
        List<ParticipationRequestDto> confirmedDtoList = confirmedRequests.stream()
                .map(requestMapper::toDto)
                .toList();
        List<ParticipationRequestDto> rejectedDtoList = rejectedRequests.stream()
                .map(requestMapper::toDto)
                .toList();
        log.info("confirmedDtoList {}", confirmedDtoList.size());
        log.info("rejectedDtoList {}", rejectedDtoList.size());
        return new EventRequestStatusUpdateResult(confirmedDtoList, rejectedDtoList);
    }

    @Transactional
    @Override
    public EventFullDto updateEventAdmin(UpdateEventAdminRequest updateEventAdminRequest, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + eventId + " не найдено"));
        if (updateEventAdminRequest.getEventDate() != null
                && updateEventAdminRequest.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Невозможно изменить дату на уже наступившую");
        }
        if (event.getPublishedOn() != null
                && event.getEventDate().isBefore(event.getPublishedOn().plusHours(1))) {
            throw new ValidationException("Дата события должна быть не позднее, чем через 1 час после публикации.");
        }
        updateIfNotNull(updateEventAdminRequest.getAnnotation(), event::setAnnotation);
        updateIfNotNull(updateEventAdminRequest.getDescription(), event::setDescription);
        updateIfNotNull(updateEventAdminRequest.getEventDate(), event::setEventDate);
        updateIfNotNull(updateEventAdminRequest.getPaid(), event::setPaid);
        updateIfNotNull(updateEventAdminRequest.getParticipantLimit(), event::setParticipantLimit);
        updateIfNotNull(updateEventAdminRequest.getRequestModeration(), event::setRequestModeration);
        updateIfNotNull(updateEventAdminRequest.getTitle(), event::setTitle);
        if (updateEventAdminRequest.getLocation() != null) {
            event.setLocation(locationRepository.save(updateEventAdminRequest.getLocation()));
        }
        if (updateEventAdminRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventAdminRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id = " + updateEventAdminRequest.getCategory() + " не найдена"));
            event.setCategory(category);
        }
        updateIfNotNull(updateEventAdminRequest.getStateAction(), action ->
                handleAdminStateAction(event, action));
        List<String> uris = List.of("/events/" + eventId);
        Long views = getViews(event.getCreatedOn(), LocalDateTime.now(), uris, true);
        return eventMapper.toFullDto(event, views);
    }

    @Transactional
    @Override
    public List<EventFullDto> findAdminEvents(List<Integer> users, List<State> states, List<Integer> categories,
                                              LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from,
                                              Integer size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        Specification<Event> specification = Specification
                .where(EventSpecification.hasInitiators(users))
                .and(EventSpecification.hasStates(states))
                .and(EventSpecification.categoryIn(categories))
                .and(EventSpecification.eventDateAfter(rangeStart))
                .and(EventSpecification.eventDateBefore(rangeEnd));

        Page<Event> events = eventRepository.findAll(specification, pageable);
        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .toList();
        Map<String, Long> viewsMap = getStats(events.getContent(), uris, true);
        return events.stream()
                .map(event -> eventMapper.toFullDto(event, viewsMap.getOrDefault("/events/" + event.getId(), 0L)))
                .toList();
    }

    @Override
    public List<EventShortDto> findEventsPublic(String text, List<Integer> categories, Boolean paid,
                                                LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                Boolean onlyAvailable, String sort, Integer from,
                                                Integer size, HttpServletRequest httpServletRequest) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Время начала позже времени окончания");
        }
        int page = from / size;
        Sort sortBy = Sort.unsorted();
        if ("EVENT_DATE".equals(sort)) {
            sortBy = Sort.by("eventDate").ascending();
        } else if ("VIEWS".equals(sort)) {
            sortBy = Sort.by("views").descending();
        }
        if (rangeEnd != null) {
            if (rangeEnd.isBefore(rangeStart)) {
                throw new ValidationException("End is before start");
            }
        }
        Pageable pageable = PageRequest.of(page, size, sortBy);
        Specification<Event> specification = Specification
                .where(EventSpecification.textInAnnotationOrDescription(text))
                .and(EventSpecification.categoryIn(categories))
                .and(EventSpecification.eventDateAfter(rangeStart))
                .and(EventSpecification.eventDateBefore(rangeEnd))
                .and(EventSpecification.isAvailable(onlyAvailable))
                .and(EventSpecification.sortBySortType(sort))
                .and(EventSpecification.onlyPublished());
        Page<Event> events = eventRepository.findAll(specification, pageable);
        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .toList();
        Map<String, Long> viewsMap = getStats(events.getContent(), uris, true);
        return events.stream()
                .map(event -> eventMapper.toShortDto(event, viewsMap.getOrDefault("/events/" + event.getId(), 0L)))
                .toList();
    }

    @Override
    public EventFullDto findEventByIdPublic(Long eventId, HttpServletRequest httpServletRequest) {
        Event event = eventRepository.findByIdAndState(eventId, State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + eventId + " не найдено"));
        ViewingRequestDto viewingRequestDto = new ViewingRequestDto("ewm-main-service",
                "/events/" + eventId, httpServletRequest.getRemoteAddr(), LocalDateTime.now());
        statsClient.addHit(viewingRequestDto);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Long views = getViews(event.getPublishedOn(), LocalDateTime.now(),
                List.of("/events/" + eventId), true);
        return eventMapper.toFullDto(event, views);
    }

    private void handleAdminStateAction(Event event, StateAction action) {
        switch (action) {
            case PUBLISH_EVENT:
                if (event.getState() != State.PENDING) {
                    throw new ConflictException("Невозможно опубликовать событие в состоянии:" + event.getState());
                }
                event.setState(State.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
                log.info("Событие с id = " + event.getId() + " опубликованно");
                break;

            case REJECT_EVENT:
                if (event.getState() == State.PUBLISHED) {
                    throw new ConflictException("Невозможно отклонить опубликованное событие");
                }
                event.setState(State.CANCELED);
                log.info("Событие с id = " + event.getId() + " отклонено");
                break;
        }
    }

    private <T> void updateIfNotNull(T newValue, Consumer<T> setter) {
        if (newValue != null) {
            setter.accept(newValue);
        }
    }

    private Long getViews(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        List<ResultStatsDto> stats = statsClient.getStats(start, end, uris, unique);
        return stats.stream()
                .findFirst()
                .map(ResultStatsDto::getHits)
                .orElse(0L);
    }

    private Map<String, Long> getStats(List<Event> events, List<String> uris, Boolean unique) {
        List<ResultStatsDto> stats;
        LocalDateTime start = events.stream()
                .map(Event::getPublishedOn)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElseGet(() -> LocalDateTime.now().minusWeeks(1));
        stats = statsClient.getStats(
                start,
                LocalDateTime.now(),
                uris,
                unique
        );
        return stats.stream()
                .collect(Collectors.toMap(
                        ResultStatsDto::getUri,
                        ResultStatsDto::getHits
                ));
    }
}
