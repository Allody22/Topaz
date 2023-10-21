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
@Builder
@NoArgsConstructor
public class UpdateOrderInfoRequest {

    @NotNull
    private Long orderId;

    @Size(max = 50)
    private String wheelR = null;

    @Size(max = 50)
    private String userPhone = null;

    @NotBlank
    @Size(max = 30)
    private String orderType = null;

    private Integer price = null;

    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date startTime = null;

    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date endTime = null;

    @Size(max = 50)
    private String administrator = null;

    @Size(max = 50)
    private String autoNumber = null;

    private Integer autoType = null;

    @Size(max = 50)
    private String specialist = null;

    private Integer boxNumber = null;

    private Integer bonuses = null;

    @Size(max = 200)
    private String comments = null;

    @Size(max = 255)
    private String sale = null;

    private List<String> orders = null;

    @Size(max = 100)
    @NotBlank
    private String currentStatus = null;
}
