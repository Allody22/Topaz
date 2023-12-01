package ru.nsu.carwash_server.payload.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import ru.nsu.carwash_server.models.secondary.helpers.TimeIntervals;

import java.util.Date;
import java.util.List;

@Data
@Setter
@AllArgsConstructor
@Schema(description = "Класс для ответа на запросы, где необходима информация о заказе")
public class FreeTimeAndBoxResponse {

    @Schema(description = "Посчитанное время выполнения заказа")
    private Integer time;

    @Schema(description = "Интервалы доступного времени")
    private List<TimeIntervals> availableTime;

    @Schema(description = "Текущее время", example = "2023-05-03T08:10:11.0+07")
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date currentTime;
}
