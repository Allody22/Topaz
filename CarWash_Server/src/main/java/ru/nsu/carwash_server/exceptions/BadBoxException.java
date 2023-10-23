package ru.nsu.carwash_server.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadBoxException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BadBoxException(int box, String orderType) {
        super(String.format("Ошибка! Бокс '%d' нельзя использовать для типа заказа '%s' !", box, orderType));
    }
}
