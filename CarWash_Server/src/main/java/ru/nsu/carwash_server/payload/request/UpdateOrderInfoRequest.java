package ru.nsu.carwash_server.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UpdateOrderInfoRequest {

    @NotNull
    private Long orderId;

    private String wheelR = null;

    private String userPhone = null;

    private String orderType = null;

    private Integer price = null;

    @JsonFormat(timezone="Asia/Novosibirsk")
    private Date startTime = null;

    @JsonFormat(timezone="Asia/Novosibirsk")
    private Date endTime = null;

    private String administrator = null;

    private String autoNumber = null;

    private Integer autoType = null;

    private String specialist = null;

    private Integer boxNumber = null;

    private Integer bonuses = null;

    private String comments = null;

    private String sale = null;

    private List<String> orders = null;

    private String currentStatus = null;
}
