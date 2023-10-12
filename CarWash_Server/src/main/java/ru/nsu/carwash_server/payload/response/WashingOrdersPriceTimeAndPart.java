package ru.nsu.carwash_server.payload.response;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WashingOrdersPriceTimeAndPart {
    private String name;

    private int priceFirstType;

    private int priceSecondType;

    private int priceThirdType;

    private int timeFirstType;

    private int timeSecondType;

    private int timeThirdType;

    private String role;

    private String associatedOrder;
}
