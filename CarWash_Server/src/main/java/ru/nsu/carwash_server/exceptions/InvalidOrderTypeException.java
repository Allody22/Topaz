package ru.nsu.carwash_server.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class InvalidOrderTypeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidOrderTypeException(String orderType) {
        super(String.format("Ошибка! Тип заказа '%s' не существует", orderType));
    }
}
