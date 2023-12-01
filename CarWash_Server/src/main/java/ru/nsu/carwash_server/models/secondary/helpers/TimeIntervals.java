package ru.nsu.carwash_server.models.secondary.helpers;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
@Schema(description = "Класс для хранения временных интервалов")
public class TimeIntervals {

    @Schema(description = "Время начала", example = "2023-05-03T08:10:11.0+07")
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date startTime;

    @Schema(description = "Время конца", example = "2023-05-03T08:10:11.0+07")
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date endTime;

    @Schema(description = "Номер бокса")
    private Integer box;
}
