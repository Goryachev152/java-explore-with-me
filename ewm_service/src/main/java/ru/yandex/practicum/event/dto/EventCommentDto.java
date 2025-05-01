package ru.yandex.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;
import ru.yandex.practicum.category.dto.CategoryDto;
import ru.yandex.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class EventCommentDto {
    String annotation;
    CategoryDto category;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;
    Long id;
    UserShortDto initiator;
    String title;
}
