package ru.yandex.practicum.user.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class UserDto {
    Long id;
    String email;
    String name;
}
