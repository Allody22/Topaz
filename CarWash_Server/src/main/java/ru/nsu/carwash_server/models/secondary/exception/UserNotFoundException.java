package ru.nsu.carwash_server.models.secondary.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public UserNotFoundException(String userName) {
        super(String.format("Пользователь " + "'" + userName + "'" + " не найден", userName));
    }
}