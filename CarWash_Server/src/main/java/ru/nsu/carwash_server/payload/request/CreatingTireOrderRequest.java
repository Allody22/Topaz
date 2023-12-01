package ru.nsu.carwash_server.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Запрос на создание шиномонтажа с сайта")
public class CreatingTireOrderRequest {

    @Schema(description = "Список услуг")
    private List<String> orders = null;

    @Schema(description = "Контакты пользователя", maxLength = 50)
    @Size(max = 50)
    private String userContacts = null;

    @Size(max = 50)
    private String wheelR = null;

    @Schema(description = "Время начала", required = true, example = "2023-05-03T08:10:11.0+07")
    @NotNull
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date startTime = null;

    @Size(max = 255)
    private String sale = null;

    @NotNull
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date endTime = null;

    @Size(max = 50)
    private String administrator = null;

    @Size(max = 50)
    private String specialist = null;

    @NotNull
    private int boxNumber;

    private int bonuses;

    @Size(max = 200)
    private String comments = null;

    @Size(max = 50)
    private String autoNumber = null;

    private int autoType;

    private Integer price = null;

    @NotBlank
    @Size(max = 100)
    private String currentStatus;
}