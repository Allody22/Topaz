package ru.nsu.carwash_server.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AllOrdersEntity {
    private String name = null;

    private Integer priceFirstType = null;

    private Integer priceSecondType = null;

    private Integer priceThirdType = null;

    private Integer timeFirstType = null;

    private Integer timeSecondType = null;

    private Integer timeThirdType = null;

    private Integer price_r_13 = null;

    private Integer price_r_14 = null;

    private Integer price_r_15 = null;

    private Integer price_r_16 = null;

    private Integer price_r_17 = null;

    private Integer price_r_18 = null;

    private Integer price_r_19 = null;

    private Integer price_r_20 = null;

    private Integer price_r_21 = null;

    private Integer price_r_22 = null;

    private Integer time_r_13 = null;

    private Integer time_r_14 = null;

    private Integer time_r_15 = null;

    private Integer time_r_16 = null;

    private Integer time_r_17 = null;

    private Integer time_r_18 = null;

    private Integer time_r_19 = null;

    private Integer time_r_20 = null;

    private Integer time_r_21 = null;

    private Integer time_r_22 = null;

    private String type = null;

    private String role = null;

    private String associatedOrder = null;
}
