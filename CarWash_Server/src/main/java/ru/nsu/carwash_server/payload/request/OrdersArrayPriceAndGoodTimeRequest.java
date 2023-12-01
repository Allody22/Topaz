package ru.nsu.carwash_server.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "Класс для запроса на получение свободного времени")
public class OrdersArrayPriceAndGoodTimeRequest {

    @Schema(description = "Тип услуг")
    @NotBlank
    @Size(max = 30)
    private String orderType = null;

    @Schema(description = "Список услуг", required = true)
    @NotNull
    @Size(min = 1)
    private List<String> orders = null;

    @Schema(description = "Радиус колеса", maxLength = 50)
    @Size(max = 50)
    private String wheelR = null;

    @Schema(description = "Тип кузова")
    private int bodyType;

    @Schema(description = "Время начала интересующего дня", required = true, example = "2023-05-03T08:10:11.0+07")
    @NotNull
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date startTime = null;

    @Schema(description = "Время конца интересующего дня", required = true, example = "2023-05-03T08:10:11.0+07")
    @NotNull
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date endTime = null;
}
