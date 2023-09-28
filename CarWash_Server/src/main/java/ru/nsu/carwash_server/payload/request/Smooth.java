package ru.nsu.carwash_server.payload.request;

import lombok.Data;

@Data
public class Smooth {
    private String stopDateTime;

    private Integer stepSeconds;
}
