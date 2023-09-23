package ru.nsu.carwash_server.payload.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdateTireServiceRequest {

    @NotBlank
    private String name = null;

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

    private String role = null;
}
