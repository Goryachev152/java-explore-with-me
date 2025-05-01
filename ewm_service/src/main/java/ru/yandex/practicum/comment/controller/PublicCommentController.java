package ru.yandex.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.comment.dto.CommentDto;
import ru.yandex.practicum.comment.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class PublicCommentController {
    private final CommentService commentService;

    @GetMapping("/{commId}")
    public CommentDto findCommentById(@PathVariable Long commId) {
        return commentService.findCommentById(commId);
    }

    @GetMapping("/events/{eventId}")
    public List<CommentDto> findCommentsByEventId(@PathVariable Long eventId) {
        return commentService.findCommentsByEventId(eventId);
    }
}
