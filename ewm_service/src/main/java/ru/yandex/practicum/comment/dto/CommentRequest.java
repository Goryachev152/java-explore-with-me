package ru.yandex.practicum.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder(toBuilder = true)
public class CommentRequest {
    @Size(min = 1, max = 7000)
    @NotBlank
    private String text;
}
