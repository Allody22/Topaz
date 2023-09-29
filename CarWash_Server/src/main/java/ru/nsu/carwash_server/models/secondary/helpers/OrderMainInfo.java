package ru.nsu.carwash_server.models.secondary.helpers;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderMainInfo {

    private Long id;

    private String orderType;

    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date dateOfCreation;

    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date startTime;

    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date endTime;

    private String autoNumber;

    private Integer autoType;

    private Integer bonuses;

    private Integer price;

    private String wheelR;

    private String comments;

    List<String> includedOrders;

    List<String> connectedOrders;

    private String currentStatus;
}
