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
public class BookingTireOrderRequest {

    @Size(min = 1)
    @NotNull
    private List<String> orders = null;

    @Size(max = 50)
    private String wheelR = null;

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

    @Size(max = 255)
    private String sale = null;

    private int bonuses;

    @Size(max = 255)
    private String comments = null;

    @Size(max = 50)
    private String autoNumber = null;

    private int autoType;

    private Integer price = null;

    @NotBlank
    @Size(max = 100)
    private String currentStatus;
}