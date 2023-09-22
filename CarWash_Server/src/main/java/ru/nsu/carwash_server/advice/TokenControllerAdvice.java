package ru.nsu.carwash_server.advice;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import ru.nsu.carwash_server.models.secondary.exception.NotInDataBaseException;
import ru.nsu.carwash_server.models.secondary.exception.TokenRefreshException;
import ru.nsu.carwash_server.models.secondary.exception.UserNotFoundException;

import java.util.Date;

@RestControllerAdvice
public class TokenControllerAdvice {


  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ErrorMessage handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request) {
    String errorMessage = "В базе данных уже существует такой элемент.\n" +
            "Это нарушает уникальность этого значения.";
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
    return new ErrorMessage(
        HttpStatus.FORBIDDEN.value(),
        new Date(),
        ex.getMessage(),
        request.getDescription(false));
  }

  @ExceptionHandler(value = NotInDataBaseException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorMessage handleNotInDataBaseException(NotInDataBaseException ex, WebRequest request) {
    return new ErrorMessage(
            HttpStatus.NOT_FOUND.value(),
            new Date(),
            ex.getMessage(),
            request.getDescription(false));
  }

  @ExceptionHandler(value = UserNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorMessage handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
    return new ErrorMessage(
            HttpStatus.NOT_FOUND.value(),
            new Date(),
            ex.getMessage(),
            request.getDescription(false));
  }
}
