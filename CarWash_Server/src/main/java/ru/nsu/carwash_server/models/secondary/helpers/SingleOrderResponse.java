package ru.nsu.carwash_server.models.secondary.helpers;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@Data
public class SingleOrderResponse {

    private Long id;

    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date dateOfCreation;

    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date startTime;

    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date endTime;

    private String administrator;

    private String specialist;

    private String autoNumber;

    private Integer autoType;

    private Integer boxNumber;

    private Integer bonuses;

    private Integer price;

    private String wheelR;

    private String comments;

    List<String> orders;

    private String userNumber;

    private String orderType;

    private String currentStatus;

    private String sale;
}

