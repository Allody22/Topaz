package ru.nsu.carwash_server.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TooManyAttemptsInCodeException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TooManyAttemptsInCodeException() {
        super("Превышено количество попыток ввода кода подтверждения в 30 минут.");
    }
}
