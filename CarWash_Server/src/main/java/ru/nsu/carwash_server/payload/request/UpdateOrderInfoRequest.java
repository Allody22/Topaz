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
@Builder
@NoArgsConstructor
@Schema(description = "Запрос на обновлении информации о заказе с сайта")
public class UpdateOrderInfoRequest {

    @Schema(description = "Айди заказа", required = true)
    @NotNull
    private Long orderId;

    @Schema(description = "Радиус колеса", maxLength = 50)
    @Size(max = 50)
    private String wheelR = null;

    @Schema(description = "Новые контакты пользователя", maxLength = 50)
    @Size(max = 50)
    private String userPhone = null;

    @Schema(description = "Тип заказа", maxLength = 30, required = true)
    @NotBlank
    @Size(max = 30)
    private String orderType = null;

    @Schema(description = "Новая цена за заказ")
    private Integer price = null;

    @Schema(description = "Новое время начала", example = "2023-05-03T08:10:11.0+07")
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date startTime = null;

    @Schema(description = "Новое время конца", example = "2023-05-03T08:10:11.0+07")
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date endTime = null;

    @Schema(description = "Новые контакты администратора", maxLength = 50)
    @Size(max = 50)
    private String administrator = null;

    @Schema(description = "Новый номер авто", maxLength = 50)
    @Size(max = 50)
    private String autoNumber = null;

    @Schema(description = "Новый тип кузова")
    private Integer autoType = null;

    @Schema(description = "Новые контакты специалиста", maxLength = 50)
    @Size(max = 50)
    private String specialist = null;

    @Schema(description = "Номер бокса")
    private Integer boxNumber = null;

    @Schema(description = "Использованные бонусы")
    private Integer bonuses = null;

    @Schema(description = "Новые комментарии к заказу", maxLength = 200)
    @Size(max = 200)
    private String comments = null;

    @Schema(description = "Новая акция для данного заказа", maxLength = 255)
    @Size(max = 255)
    private String sale = null;

    @Schema(description = "Новый набор услуг")
    private List<String> orders = null;

    @Schema(description = "Новый статус заказа", required = true, maxLength = 100)
    @Size(max = 100)
    @NotBlank
    private String currentStatus = null;
}
