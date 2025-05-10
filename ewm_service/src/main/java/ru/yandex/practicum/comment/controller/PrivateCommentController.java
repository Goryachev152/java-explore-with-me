package ru.yandex.practicum.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.comment.dto.CommentDto;
import ru.yandex.practicum.comment.dto.CommentRequest;
import ru.yandex.practicum.comment.service.CommentService;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class PrivateCommentController {
    private final CommentService commentService;

    @PostMapping("/users/{userId}/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(@PathVariable Long userId, @PathVariable Long eventId,
                             @Valid @RequestBody CommentRequest commentRequest) {
        return commentService.create(userId, eventId, commentRequest);
    }

    @PatchMapping("/users/{userId}/comments/{commId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto update(@PathVariable Long userId, @PathVariable Long commId,
                                     @Valid @RequestBody CommentRequest dto) {
        return commentService.update(userId, commId, dto);
    }

    @DeleteMapping("/users/{userId}/comments/{commId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId, @PathVariable Long commId) {
        commentService.delete(userId, commId);
    }
}
