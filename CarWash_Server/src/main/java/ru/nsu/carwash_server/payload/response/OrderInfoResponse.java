package ru.nsu.carwash_server.payload.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Класс для ответа на запросы, где необходима информация о заказе")
public class OrderInfoResponse {

    @Schema(description = "Айди заказа")
    private Long id;

    @Schema(description = "Список услуг")
    private List<String> orders;

    @Schema(description = "Время начала", required = true, example = "2023-05-03T08:10:11.0+07")
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date startTime;

    @Schema(description = "Время конца", required = true, example = "2023-05-03T08:10:11.0+07")
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date endTime;

    @Schema(description = "Контакты администратора")
    private String administrator;

    @Schema(description = "Контакты специалиста")
    private String specialist;

    @Schema(description = "Номер бокса")
    private int boxNumber;

    @Schema(description = "Номер авто")
    private String autoNumber;

    @Schema(description = "Тип кузова")
    private int autoType;

    @Schema(description = "Бонусы, если имеются")
    private int bonuses;

    @Schema(description = "Комментарии к заказу")
    private String comments;

    @Schema(description = "Айди пользователя который делал заказ")
    private Long userId;

    @Schema(description = "Цена за заказ")
    private int price;

    @Schema(description = "Тип услуги")
    private String orderType;

    @Schema(description = "Радиус колес")
    private String wheelR;

    @Schema(description = "Текущий статус заказа")
    private String currentStatus;

    @Schema(description = "Контакты пользователя")
    private String userContacts;

    public OrderInfoResponse(Long id, List<String> orders, Date startTime, Date endTime,
                             String administrator, String specialist, int boxNumber,
                             String autoNumber, int autoType, int bonuses,
                             String comments, Long userId, int price,
                             String orderType, String wheelR, String currentStatus) {
        this.id = id;
        this.orders = orders;
        this.currentStatus = currentStatus;
        this.startTime = startTime;
        this.endTime = endTime;
        this.administrator = administrator;
        this.specialist = specialist;
        this.boxNumber = boxNumber;
        this.autoNumber = autoNumber;
        this.autoType = autoType;
        this.bonuses = bonuses;
        this.comments = comments;
        this.userId = userId;
        this.price = price;
        this.orderType = orderType;
        this.wheelR = wheelR;
    }

    public OrderInfoResponse(Long id, List<String> orders, Date startTime, Date endTime, String administrator,
                             String specialist, int boxNumber, String autoNumber, int autoType,
                             int bonuses, String comments, int price, String orderType,
                             String wheelR, String userContacts, String currentStatus) {
        this.id = id;
        this.orders = orders;
        this.startTime = startTime;
        this.endTime = endTime;
        this.administrator = administrator;
        this.specialist = specialist;
        this.boxNumber = boxNumber;
        this.autoNumber = autoNumber;
        this.autoType = autoType;
        this.bonuses = bonuses;
        this.currentStatus = currentStatus;
        this.comments = comments;
        this.price = price;
        this.orderType = orderType;
        this.wheelR = wheelR;
        this.userContacts = userContacts;
    }
}
