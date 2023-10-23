package ru.nsu.carwash_server.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyExistException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public UserAlreadyExistException() {
        super("Пользователь уже существует");
    }
}
