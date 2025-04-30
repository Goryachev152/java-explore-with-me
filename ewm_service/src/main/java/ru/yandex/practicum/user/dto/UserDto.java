package ru.yandex.practicum.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder(toBuilder = true)
public class UserDto {
    Long id;
    String email;
    String name;
}
