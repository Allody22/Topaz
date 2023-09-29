package ru.nsu.carwash_server.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingTireOrderRequest {

    private List<String> orders = null;

    private String wheelR = null;

    @NotNull
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date startTime = null;

    @NotNull
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date endTime = null;

    private String administrator = null;

    private String specialist = null;

    @NotNull
    private int boxNumber;

    private String sale = null;

    private int bonuses;

    private String comments = null;

    private String autoNumber = null;

    private int autoType;

    private Integer price = null;

    @NotBlank
    private String currentStatus;
}