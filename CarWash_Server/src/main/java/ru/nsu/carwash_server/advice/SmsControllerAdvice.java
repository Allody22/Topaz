package ru.nsu.carwash_server.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.nsu.carwash_server.exceptions.SMSException;
import ru.nsu.carwash_server.exceptions.TooManyAttemptsInCodeException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SmsControllerAdvice {

    @ExceptionHandler(SMSException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Map<String, String> handleTooManySMSException(SMSException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return errorResponse;
    }

    @ExceptionHandler(TooManyAttemptsInCodeException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleTooManyAttemptsInCodeException(TooManyAttemptsInCodeException ex) {
        log.error("TOO MANY ATTEMPTS IN CODE ADVICE");
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return errorResponse;
    }

}
