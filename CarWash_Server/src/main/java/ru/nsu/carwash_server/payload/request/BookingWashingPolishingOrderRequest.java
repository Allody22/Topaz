package ru.nsu.carwash_server.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingWashingPolishingOrderRequest {

    @Size(min = 1)
    @NotNull
    private List<String> orders;

    @NotNull
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date startTime = null;

    @NotNull
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date endTime = null;

    @Size(max = 50)
    private String administrator = null;

    @Size(max = 50)
    private String specialist = null;

    @NotNull
    private int boxNumber;

    private int bonuses;

    @Size(max = 200)
    private String comments = null;

    @Size(max = 50)
    private String autoNumber = null;

    private int autoType;

    @Size(max = 255)
    private String sale = null;

    private Integer price = null;

    @Size(max = 30)
    private String orderType = null;

    @NotBlank
    @Size(max = 100)
    private String currentStatus;
}
