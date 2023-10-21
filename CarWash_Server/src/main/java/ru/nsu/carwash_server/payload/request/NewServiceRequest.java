package ru.nsu.carwash_server.payload.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class NewServiceRequest {

    @NotBlank
    private String serviceType = null;

    @NotBlank
    @Size(max = 120)
    private String name = null;

    @PositiveOrZero
    private Integer priceFirstType = null;

    @PositiveOrZero
    private Integer priceSecondType = null;

    @PositiveOrZero
    private Integer priceThirdType = null;

    @PositiveOrZero
    private Integer timeFirstType = null;

    @PositiveOrZero
    private Integer timeSecondType = null;

    @PositiveOrZero
    private Integer timeThirdType = null;

    @PositiveOrZero
    private Integer price_r_13 = null;

    @PositiveOrZero
    private Integer price_r_14 = null;

    @PositiveOrZero
    private Integer price_r_15 = null;

    @PositiveOrZero
    private Integer price_r_16 = null;

    @PositiveOrZero
    private Integer price_r_17 = null;

    @PositiveOrZero
    private Integer price_r_18 = null;

    @PositiveOrZero
    private Integer price_r_19 = null;

    @PositiveOrZero
    private Integer price_r_20 = null;

    @PositiveOrZero
    private Integer price_r_21 = null;

    @PositiveOrZero
    private Integer price_r_22 = null;

    @PositiveOrZero
    private Integer time_r_13 = null;

    @PositiveOrZero
    private Integer time_r_14 = null;

    @PositiveOrZero
    private Integer time_r_15 = null;

    @PositiveOrZero
    private Integer time_r_16 = null;

    @PositiveOrZero
    private Integer time_r_17 = null;

    @PositiveOrZero
    private Integer time_r_18 = null;

    @PositiveOrZero
    private Integer time_r_19 = null;

    @PositiveOrZero
    private Integer time_r_20 = null;

    @PositiveOrZero
    private Integer time_r_21 = null;

    @PositiveOrZero
    private Integer time_r_22 = null;

    private String role = null;

    private List<String> includedIn = null;
}
