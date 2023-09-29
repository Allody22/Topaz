package ru.nsu.carwash_server.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
public class CreatingWashingOrder {

    private List<String> orders = null;

    @NotNull
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date startTime = null;

    private String userContacts = null;

    @NotNull
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date endTime = null;

    private String administrator = null;

    private String specialist = null;

    private String sale = null;

    @NotNull
    private Integer boxNumber = null;

    private Integer bonuses = null;

    private String comments = null;

    private String autoNumber = null;

    private int autoType;

    private Integer price = null;

    @NotBlank
    private String currentStatus = null;
}
