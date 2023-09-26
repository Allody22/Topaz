package ru.nsu.carwash_server.payload.request;

import lombok.Data;

@Data
public class Smooth {
    private String stopDateTime = null;

    private Integer stepSeconds = null;
}
