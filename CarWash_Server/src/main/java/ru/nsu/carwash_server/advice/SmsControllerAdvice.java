package ru.nsu.carwash_server.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.nsu.carwash_server.exceptions.BadRequestException;
import ru.nsu.carwash_server.payload.response.MessageResponse;

@RestControllerAdvice
public class SmsControllerAdvice {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequestException(BadRequestException ex) {
        return ResponseEntity.badRequest().body(new MessageResponse(ex.getMessage()));
    }
}
