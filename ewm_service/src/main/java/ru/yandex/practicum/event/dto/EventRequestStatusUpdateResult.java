package ru.yandex.practicum.event.dto;

import lombok.Builder;
import lombok.Value;
import ru.yandex.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class EventRequestStatusUpdateResult {
    List<ParticipationRequestDto> confirmedRequests;
    List<ParticipationRequestDto> rejectedRequests;
}
