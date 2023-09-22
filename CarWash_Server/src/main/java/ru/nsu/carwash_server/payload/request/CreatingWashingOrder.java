package ru.nsu.carwash_server.payload.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
public class CreatingWashingOrder {
    private List<String> orders;

    @NotNull
    private Date startTime;

    private String userContacts;

    @NotNull
    private Date endTime;

    private String administrator;

    private String specialist;

    private String sale = null;

    private int boxNumber;

    private int bonuses;

    private String comments;

    private String autoNumber;

    private int autoType;

    private Integer price = null;

    @NotNull
    private String currentStatus;
}
