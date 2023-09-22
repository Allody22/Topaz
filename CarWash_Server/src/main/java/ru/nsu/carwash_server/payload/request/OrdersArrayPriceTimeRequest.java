package ru.nsu.carwash_server.payload.request;

import lombok.Data;

import java.util.ArrayList;

@Data
public class OrdersArrayPriceTimeRequest {
    String orderType;
    ArrayList<String> orders;
    String wheelR;
    int bodyType;
}
