package ru.yandex.practicum.comment.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.comment.dto.CommentDto;
import ru.yandex.practicum.comment.dto.CommentRequest;
import ru.yandex.practicum.comment.mapper.CommentMapper;
import ru.yandex.practicum.comment.model.Comment;
import ru.yandex.practicum.comment.repository.CommentRepository;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.model.State;
import ru.yandex.practicum.event.repository.EventRepository;
import ru.yandex.practicum.exception.ConflictException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.user.model.User;
import ru.yandex.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;

    @Transactional
    @Override
    public CommentDto create(Long userId, Long eventId, CommentRequest commentRequest) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id= " + userId + " не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id= " + eventId + " не найдено"));
        if (event.getState() != State.PUBLISHED) {
            throw new ValidationException("Событие c id= " + eventId + " не опубликовано");
        }
        Comment comment = commentMapper.toComment(commentRequest, event, author, LocalDateTime.now());
        comment = commentRepository.save(comment);
        log.info("Комментарий с id= {} добавлен", comment.getId());
        return commentMapper.toCommentDto(comment);
    }

    @Transactional
    @Override
    public CommentDto update(Long userId, Long commId, CommentRequest commentRequest) {
        Comment comment = commentRepository.findById(commId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id= " + commId + " не найден"));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Пользователь с id= " + userId + " не является автором комментария с id= " + commId);
        }
        comment.setText(commentRequest.getText());
        commentRepository.save(comment);
        return commentMapper.toCommentDto(comment);
    }

    @Transactional
    @Override
    public void delete(Long userId, Long commId) {
        Comment comment = commentRepository.findById(commId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id= " + commId + " не найден"));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Пользователь с id= " + userId + " не является автором комментария с id= " + commId);
        }
        commentRepository.delete(comment);
        log.info("Комментарий с id= {} был удален", commId);
    }

    @Transactional
    @Override
    public void deleteCommentAdmin(Long commId) {
        if(!commentRepository.existsById(commId)) {
            throw new NotFoundException("Комментарий с id= " + commId + " не найден");
        }
        commentRepository.deleteById(commId);
    }

    @Override
    public CommentDto findCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id= " + commentId + " не найден"));
        return commentMapper.toCommentDto(comment);
    }

    @Override
    public List<CommentDto> findCommentsByEventId(Long eventId) {
        List<Comment> comments = commentRepository.findAllByEventId(eventId);
        return comments.stream().map(commentMapper::toCommentDto).toList();
    }
}
