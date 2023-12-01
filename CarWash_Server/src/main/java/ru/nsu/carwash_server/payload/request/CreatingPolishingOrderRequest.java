package ru.nsu.carwash_server.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "Запрос на создание полировки с сайта")
public class CreatingPolishingOrderRequest {

    @Schema(description = "Список услуг")
    private List<String> orders = null;

    @Schema(description = "Время начала", required = true, example = "2023-05-03T08:10:11.0+07")
    @NotNull
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date startTime = null;

    @Schema(description = "Контакты пользователя", maxLength = 50)
    @Size(max = 50)
    private String userContacts = null;

    @Schema(description = "Время конца", required = true, example = "2023-05-03T08:10:11.0+07")
    @NotNull
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date endTime = null;

    @Schema(description = "Контакты администратора", maxLength = 50)
    @Size(max = 50)
    private String administrator = null;

    @Schema(description = "Контакты специалиста", maxLength = 50)
    @Size(max = 50)
    private String specialist = null;

    @Schema(description = "Номер бокса")
    @NotNull
    private int boxNumber;

    @Schema(description = "Акция для данного заказа", maxLength = 255)
    @Size(max = 255)
    private String sale = null;

    @Schema(description = "Бонусы, если имеются")
    private int bonuses;

    @Schema(description = "Комментарии к заказу", maxLength = 200)
    @Size(max = 200)
    private String comments = null;

    @Schema(description = "Номер авто", maxLength = 50)
    @Size(max = 50)
    private String autoNumber = null;

    @Schema(description = "Тип кузова")
    private int autoType;

    @Schema(description = "Цена за заказ", required = true)
    private Integer price = null;

    @Schema(description = "Текущий статус заказа", required = true, maxLength = 100)
    @NotBlank
    @Size(max = 100)
    private String currentStatus = null;
}
