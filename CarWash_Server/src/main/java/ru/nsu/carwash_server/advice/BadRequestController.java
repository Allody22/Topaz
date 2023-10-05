package ru.nsu.carwash_server.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.nsu.carwash_server.exceptions.BadRequestException;
import ru.nsu.carwash_server.payload.response.MessageResponse;

@ControllerAdvice
@Slf4j
public class BadRequestController {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequestException(BadRequestException ex) {
        log.warn("Bad request exception: {}", ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(new MessageResponse(ex.getMessage()));
    }
}
