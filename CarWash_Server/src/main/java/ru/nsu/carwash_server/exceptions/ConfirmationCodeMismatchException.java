package ru.nsu.carwash_server.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class ConfirmationCodeMismatchException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ConfirmationCodeMismatchException(String userPhone) {
        super(String.format("Ошибка: код подтверждения для телефона '%s' не совпадает!", userPhone));
    }
}
