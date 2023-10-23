package ru.nsu.carwash_server.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.CONFLICT)
public class TimeSlotUnavailableException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TimeSlotUnavailableException(int box) {
        super(String.format("Ошибка! Выбранное время в боксе '%d' уже занято!", box));
    }
}
