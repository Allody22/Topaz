package ru.nsu.carwash_server.payload.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class OrdersArrayPriceTimeRequest {

    @Size(max = 30)
    private String orderType = null;

    @Size(min = 1)
    @NotNull
    private List<String> orders = null;

    @Size(max = 50)
    private String wheelR = null;

    int bodyType;
}
