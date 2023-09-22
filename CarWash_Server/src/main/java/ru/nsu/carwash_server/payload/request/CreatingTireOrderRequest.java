package ru.nsu.carwash_server.payload.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreatingTireOrderRequest {

    private List<String> orders;

    private String userContacts;

    private String wheelR;

    @NotNull
    private Date startTime;

    private String sale = null;

    @NotNull
    private Date endTime;

    private String administrator;

    private String specialist;

    private int boxNumber;

    private int bonuses;

    private String comments;

    private String autoNumber;

    private int autoType;

    private boolean executed;

    private Integer price = null;

    @NotNull
    private String currentStatus;
}