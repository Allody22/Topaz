package ru.nsu.carwash_server.models.secondary.helpers;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class OrderPriceTimeDoneTypeInfo {

    private String orderType;

    private int price;

    private Long id;

    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date startTime;

    private String currentStatus;
}
