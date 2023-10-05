package ru.nsu.carwash_server.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import ru.nsu.carwash_server.exceptions.TokenRefreshException;
import ru.nsu.carwash_server.exceptions.model.ErrorMessage;

import java.util.Date;

@RestControllerAdvice
@Slf4j
public class TokenControllerAdvice {

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorMessage handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request) {
        String errorMessage = "В базе данных уже существует такой элемент.\n" +
                "Это нарушает уникальность этого значения.";

        log.error("Data integrity violation: {} | Request: {}", ex.getMessage(), request.getDescription(true), ex);

        return new ErrorMessage(
                HttpStatus.CONFLICT.value(),
                new Date(),
                errorMessage,
                request.getDescription(false)
        );
    }

    @ExceptionHandler(value = TokenRefreshException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorMessage handleTokenRefreshException(TokenRefreshException ex, WebRequest request) {
        log.error("Token refresh exception: {} | Request: {}", ex.getMessage(), request.getDescription(true), ex);

        return new ErrorMessage(
                HttpStatus.FORBIDDEN.value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(false));
    }
}
