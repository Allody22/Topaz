package ru.nsu.carwash_server.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetBookedOrdersInTimeIntervalRequest {

    @NotNull
    private Date startTime;

    @NotNull
    private Date endTime;
}
