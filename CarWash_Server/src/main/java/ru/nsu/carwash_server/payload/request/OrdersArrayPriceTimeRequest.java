package ru.nsu.carwash_server.payload.request;

import lombok.Data;

import java.util.ArrayList;

@Data
public class OrdersArrayPriceTimeRequest {

    String orderType = null;

    ArrayList<String> orders = null;

    String wheelR = null;

    int bodyType;
}
