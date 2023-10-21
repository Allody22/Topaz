package ru.nsu.carwash_server.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class SMSException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SMSException() {
        super("Превышено количество запросов за час.");
    }
}
