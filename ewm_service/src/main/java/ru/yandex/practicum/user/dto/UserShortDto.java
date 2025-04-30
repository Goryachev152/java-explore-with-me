package ru.yandex.practicum.user.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class UserShortDto {
    Long id;
    String name;
}
