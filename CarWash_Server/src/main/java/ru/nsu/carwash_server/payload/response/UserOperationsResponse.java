package ru.nsu.carwash_server.payload.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class UserOperationsResponse {

    private String name;

    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date dateOfCreation;

    private int version;

    private String changes;
}
