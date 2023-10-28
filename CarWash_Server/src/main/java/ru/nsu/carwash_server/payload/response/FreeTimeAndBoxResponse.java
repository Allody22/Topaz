package ru.nsu.carwash_server.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import ru.nsu.carwash_server.models.secondary.helpers.TimeIntervals;

import java.util.List;

@Data
@Setter
@AllArgsConstructor
public class FreeTimeAndBoxResponse {
    private Integer time;

    private List<TimeIntervals> availableTime;
}
