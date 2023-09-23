package ru.nsu.carwash_server.payload.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdateWashingServiceRequest {

    @NotBlank
    private String name;

    private Integer priceFirstType = null;

    private Integer priceSecondType = null;

    private Integer priceThirdType = null;

    private Integer timeFirstType = null;

    private Integer timeSecondType = null;

    private Integer timeThirdType = null;

    private String role = null;

    private String includedIn = null;
}
