package ru.nsu.carwash_server.payload.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderInfoResponse {

    private Long orderId;

    private Double price;

    private String name;

    @JsonFormat(timezone="Asia/Novosibirsk")
    private Date startTime;

    private String administrator;

    private String specialist;

    private Long autoId;

    private int boxNumber;

    private int bonuses;

    private String comments;

    private boolean executed;

    @JsonFormat(timezone="Asia/Novosibirsk")
    private Date endTime;
}
