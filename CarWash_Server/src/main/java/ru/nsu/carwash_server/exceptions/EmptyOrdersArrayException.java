package ru.nsu.carwash_server.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EmptyOrdersArrayException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public EmptyOrdersArrayException() {
        super("Ошибка! Список с услугами не может быть пустой.");
    }
}