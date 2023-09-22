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
public class BookingWashingPolishingOrderRequest {

    private List<String> orders;

    @NotNull
    private Date startTime;

    @NotNull
    private Date endTime;

    private String administrator;

    private String specialist;

    private int boxNumber;

    private int bonuses;

    private String comments;

    private String autoNumber;

    private int autoType;

    private String sale = null;

    private boolean executed;

    private Integer price = null;

    private String orderType;

    @NotNull
    private String currentStatus;
}
