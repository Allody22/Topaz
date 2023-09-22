package ru.nsu.carwash_server.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class MainAndAdditionalResponse {

    List<String> mainOrders;

    List<String> additionalOrders;
}
