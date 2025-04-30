package ru.yandex.practicum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(final ValidationException e) {
        return ErrorResponse.builder()
                .message(e.getMessage())
                .reason("Incorrectly made request.")
                .status(HttpStatus.BAD_REQUEST.toString())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleRunTimeException(final RuntimeException e) {
        return ErrorResponse.builder()
                .message(e.getMessage())
                .reason("Неизвестная ошибка")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                .build();
    }
}
