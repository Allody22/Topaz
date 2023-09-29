package ru.nsu.carwash_server.payload.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class OperationsResponse {

    private Long id;

    private String username;

    private String description;

    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date dateOfCreation;
}
