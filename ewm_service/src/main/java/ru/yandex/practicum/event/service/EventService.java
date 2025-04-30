package ru.yandex.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.yandex.practicum.event.dto.EventFullDto;
import ru.yandex.practicum.event.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.event.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.event.dto.EventShortDto;
import ru.yandex.practicum.event.dto.NewEventDto;
import ru.yandex.practicum.event.dto.UpdateEventAdminRequest;
import ru.yandex.practicum.event.dto.UpdateEventUserRequest;
import ru.yandex.practicum.event.model.State;
import ru.yandex.practicum.request.dto.ParticipationRequestDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    EventFullDto createEvent(NewEventDto newEventDto,Long userId);

    EventFullDto updateEventPrivate(UpdateEventUserRequest updateEventUserRequest, Long userId, Long eventId);

    EventFullDto findByUserIdAndEventId(Long userId, Long eventId);

    List<EventShortDto> findEventByUserId(Long userId, Integer from, Integer size);

    List<ParticipationRequestDto> findRequestEventPrivate(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestEventPrivate(EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest,
                                                             Long userId, Long eventId);

    EventFullDto updateEventAdmin(UpdateEventAdminRequest updateEventAdminRequest, Long eventId);

    List<EventFullDto> findAdminEvents(List<Integer> users, List<State> states,
                                       List<Integer> categories,
                                       LocalDateTime rangeStart,
                                       LocalDateTime rangeEnd,
                                       Integer from,
                                       Integer size);

    List<EventShortDto> findEventsPublic(String text, List<Integer> categories,
                                         Boolean paid, LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd, Boolean onlyAvailable,
                                         String sort, Integer from, Integer size, HttpServletRequest httpServletRequest);

    EventFullDto findEventByIdPublic(Long eventId, HttpServletRequest httpServletRequest);
}
