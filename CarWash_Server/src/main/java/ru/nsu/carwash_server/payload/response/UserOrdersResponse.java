package ru.nsu.carwash_server.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.nsu.carwash_server.models.secondary.helpers.OrderPriceTimeDoneTypeInfo;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserOrdersResponse {
    private List<OrderPriceTimeDoneTypeInfo> orders;
}
