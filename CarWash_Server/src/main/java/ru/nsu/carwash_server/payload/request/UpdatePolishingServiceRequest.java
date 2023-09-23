package ru.nsu.carwash_server.payload.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdatePolishingServiceRequest {

    @NotBlank
    private String name = null;

    private Integer priceFirstType = null;

    private Integer priceSecondType = null;

    private Integer priceThirdType = null;

    private Integer timeFirstType = null;

    private Integer timeSecondType = null;

    private Integer timeThirdType = null;
}
