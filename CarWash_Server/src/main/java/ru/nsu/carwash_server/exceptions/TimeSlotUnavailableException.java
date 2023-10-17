package ru.nsu.carwash_server.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class TimeSlotUnavailableException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TimeSlotUnavailableException(int box) {
        super(String.format("Ошибка: выбранное время в боксе '%d' уже занято!", box));
    }
}
