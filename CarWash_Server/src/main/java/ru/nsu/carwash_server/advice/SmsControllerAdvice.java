package ru.nsu.carwash_server.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.nsu.carwash_server.exceptions.BadRequestException;
import ru.nsu.carwash_server.payload.response.MessageResponse;

@RestControllerAdvice
@Slf4j
public class SmsControllerAdvice {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequestException(BadRequestException ex) {
        log.error("Sms controller exception: ", ex);
        return ResponseEntity.badRequest().body(new MessageResponse(ex.getMessage()));
    }
}
