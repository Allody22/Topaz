package ru.nsu.carwash_server.models.secondary.helpers;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class TimeIntervals {

    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date startTime;

    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date endTime;

    private Integer box;
}
