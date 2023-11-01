package ru.nsu.carwash_server.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.nsu.carwash_server.models.secondary.helpers.SingleOrderResponse;

import java.util.List;

@Data
@AllArgsConstructor
public class OrdersArrayResponse {

    List<SingleOrderResponse> orders;
}
