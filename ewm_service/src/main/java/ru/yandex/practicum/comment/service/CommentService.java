package ru.yandex.practicum.comment.service;

import ru.yandex.practicum.comment.dto.CommentDto;
import ru.yandex.practicum.comment.dto.CommentRequest;

import java.util.List;

public interface CommentService {

    CommentDto create(Long userId, Long eventId, CommentRequest commentRequest);

    CommentDto update(Long userId, Long commId, CommentRequest commentRequest);

    void delete(Long userId, Long commId);

    void deleteCommentAdmin(Long commId);

    CommentDto findCommentById(Long commentId);

    List<CommentDto> findCommentsByEventId(Long eventId);
}
