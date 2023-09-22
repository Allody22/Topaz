package ru.nsu.carwash_server.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class ConnectedOrdersResponse {

    List<String> includedOrders;

    List<String> connectedOrders;
}
