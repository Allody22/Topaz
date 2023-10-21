package ru.nsu.carwash_server.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Data
public class CreatingWashingOrder {

    private List<String> orders = null;

    @NotNull
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date startTime = null;

    @Size(max = 50)
    private String userContacts = null;

    @NotNull
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date endTime = null;

    @Size(max = 50)
    private String administrator = null;

    @Size(max = 50)
    private String specialist = null;

    @Size(max = 255)
    private String sale = null;

    @NotNull
    private Integer boxNumber = null;

    private Integer bonuses = null;

    @Size(max = 200)
    private String comments = null;

    @Size(max = 50)
    private String autoNumber = null;

    private int autoType;

    private Integer price = null;

    @NotBlank
    @Size(max = 100)
    private String currentStatus = null;
}
