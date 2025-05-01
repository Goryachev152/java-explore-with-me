package ru.yandex.practicum.user.dto;

import ru.yandex.practicum.user.model.User;

public class UserMapper {

    public static User mapToUser(NewUserRequest newUserRequest) {
        return User.builder()
                .email(newUserRequest.getEmail())
                .name(newUserRequest.getName())
                .build();
    }

    public static UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public static UserShortDto mapToUserShort(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}
