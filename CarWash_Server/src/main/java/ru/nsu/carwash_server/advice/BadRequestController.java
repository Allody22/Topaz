package ru.nsu.carwash_server.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.nsu.carwash_server.exceptions.BadRequestException;
import ru.nsu.carwash_server.payload.response.MessageResponse;

@ControllerAdvice
public class BadRequestController {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequestException(BadRequestException ex) {
        return ResponseEntity.badRequest().body(new MessageResponse(ex.getMessage()));
    }
}
