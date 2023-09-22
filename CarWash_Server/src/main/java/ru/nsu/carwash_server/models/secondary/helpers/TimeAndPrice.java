package ru.nsu.carwash_server.models.secondary.helpers;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TimeAndPrice {
    Integer time;
    Integer price;
}
