package ru.yandex.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.event.dto.EventFullDto;
import ru.yandex.practicum.event.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.event.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.event.dto.EventShortDto;
import ru.yandex.practicum.event.dto.NewEventDto;
import ru.yandex.practicum.event.dto.UpdateEventUserRequest;
import ru.yandex.practicum.event.service.EventService;
import ru.yandex.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class PrivateEventController {
    private final EventService eventService;

    @PostMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@Valid @RequestBody NewEventDto newEventDto, @PathVariable Long userId) {
        return eventService.createEvent(newEventDto, userId);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEventPrivate(@Valid @RequestBody UpdateEventUserRequest updateEventUserRequest,
                                    @PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.updateEventPrivate(updateEventUserRequest, userId, eventId);
    }

    @GetMapping("/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto findByUserIdAndEventId(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.findByUserIdAndEventId(userId, eventId);
    }

    @GetMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> findEventByUserId(@PathVariable Long userId,
                                                 @RequestParam(defaultValue = "0") Integer from,
                                                 @RequestParam(defaultValue = "10") Integer size) {
        return eventService.findEventByUserId(userId, from, size);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> findRequestEventPrivate(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.findRequestEventPrivate(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult updateRequestEventPrivate(@PathVariable Long userId, @PathVariable Long eventId,
                                                                    @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest
                                                                    ) {
        return eventService.updateRequestEventPrivate(eventRequestStatusUpdateRequest, userId, eventId);
    }
}
