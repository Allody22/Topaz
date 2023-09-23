package ru.nsu.carwash_server.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(timezone="Asia/Novosibirsk")
    private Date startTime = null;

    @NotNull
    @JsonFormat(timezone="Asia/Novosibirsk")
    private Date endTime  = null ;
}
