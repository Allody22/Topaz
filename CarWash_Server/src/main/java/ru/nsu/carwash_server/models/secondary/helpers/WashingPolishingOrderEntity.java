package ru.nsu.carwash_server.models.secondary.helpers;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WashingPolishingOrderEntity {
    private String name;

    private int priceFirstType;

    private int priceSecondType;

    private int priceThirdType;

    private int timeFirstType;

    private int timeSecondType;

    private int timeThirdType;

    private String role;
}
