package ru.nsu.carwash_server.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class InvalidOrderTypeException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidOrderTypeException(String orderType) {
        super(String.format("Ошибка! Тип заказа '%s' не существует", orderType));
    }
}
