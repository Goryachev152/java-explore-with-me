package ru.yandex.practicum.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.exception.ConflictException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.user.dto.NewUserRequest;
import ru.yandex.practicum.user.dto.UserDto;
import ru.yandex.practicum.user.dto.UserMapper;
import ru.yandex.practicum.user.model.User;
import ru.yandex.practicum.user.repository.UserRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Transactional
    @Override
    public UserDto createUser(NewUserRequest newUserRequest) {
        if (repository.existsByEmail(newUserRequest.getEmail())) {
            throw new ConflictException("Пользователь с таким email " + newUserRequest.getEmail() + " уже существует");
        }
        User newUser = UserMapper.mapToUser(newUserRequest);
        UserDto userDto = UserMapper.mapToUserDto(repository.save(newUser));
        log.info("Пользователь {} добавлен в сервис", newUserRequest.toString());
        return userDto;
    }

    @Override
    public List<UserDto> getUsers(List<Integer> ids, Integer from, Integer size) {
        if (from < 0 || size < 0) {
            throw new ValidationException("Параметры from и size не могут быль отрицательным числом");
        }
        Pageable pageable = PageRequest.of(from / size, size);
        Page<User> users;
        if (ids == null) {
            users = repository.findAll(pageable);
        } else {
            users = repository.findByIdIn(ids, pageable);
        }
        return users.stream().map(UserMapper::mapToUserDto).toList();
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        if (!repository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        repository.deleteById(userId);
        log.info("Пользователь с id = {} удален из сервиса", userId);
    }
}
