package ru.nsu.carwash_server.payload.request;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.nsu.carwash_server.exceptions.validation.fields.NotZero;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Класс для запроса на получение свободного времени без проверки длительности услуг")
public class FreeTimeRequest {

    @Schema(description = "Тип услуг")
    @NotBlank
    @Size(max = 30)
    private String orderType = null;

    @Schema(description = "Время выполнения услуг")
    @NotNull
    @NotZero
    private int orderTime;

    @Schema(description = "Время начала интересующего дня", required = true, example = "2023-05-03T08:10:11.0+07")
    @NotNull
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date startTime = null;

    @Schema(description = "Время конца интересующего дня", required = true, example = "2023-05-03T08:10:11.0+07")
    @NotNull
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date endTime = null;

}
