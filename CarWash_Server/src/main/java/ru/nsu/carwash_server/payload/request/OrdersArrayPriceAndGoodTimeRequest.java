package ru.nsu.carwash_server.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Data
public class OrdersArrayPriceAndGoodTimeRequest {

    @NotBlank
    @Size(max = 30)
    private String orderType = null;

    @NotNull
    @Size(min = 1)
    private List<String> orders = null;

    @Size(max = 50)
    private String wheelR = null;

    int bodyType;

    @NotNull
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date startTime = null;

    @NotNull
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date endTime = null;
}
