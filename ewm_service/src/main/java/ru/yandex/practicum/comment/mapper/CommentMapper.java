package ru.yandex.practicum.comment.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.yandex.practicum.comment.dto.CommentDto;
import ru.yandex.practicum.comment.dto.CommentRequest;
import ru.yandex.practicum.comment.model.Comment;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.user.model.User;

import java.time.LocalDateTime;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, builder = @Builder(disableBuilder = false))
public interface CommentMapper {

    CommentDto toCommentDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", source = "event")
    @Mapping(target = "author", source = "author")
    @Mapping(target = "createdDate", source = "createdDate")
    Comment toComment(CommentRequest commentRequest, Event event, User author,
                      LocalDateTime createdDate);
}
