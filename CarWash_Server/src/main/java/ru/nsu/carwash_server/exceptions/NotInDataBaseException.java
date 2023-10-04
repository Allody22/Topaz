package ru.nsu.carwash_server.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotInDataBaseException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public NotInDataBaseException(String message, String name) {
        super(String.format("В базе данных " + message + name, message, name));
    }
}