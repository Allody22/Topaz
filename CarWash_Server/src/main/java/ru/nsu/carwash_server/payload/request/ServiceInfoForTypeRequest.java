package ru.nsu.carwash_server.payload.request;


import lombok.Data;

@Data
public class ServiceInfoForTypeRequest {

    private String orderName;

    private String orderType;
}
