package ru.nsu.carwash_server.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.carwash_server.exceptions.TooManyAttemptsInCodeException;
import ru.nsu.carwash_server.models.orders.OrderVersions;
import ru.nsu.carwash_server.models.secondary.helpers.OrderMainInfo;
import ru.nsu.carwash_server.models.users.Role;
import ru.nsu.carwash_server.models.users.User;
import ru.nsu.carwash_server.models.users.UserVersions;
import ru.nsu.carwash_server.payload.request.UpdateUserInfoRequest;
import ru.nsu.carwash_server.payload.request.UpdateUserPasswordRequest;
import ru.nsu.carwash_server.payload.response.ConnectedOrdersResponse;
import ru.nsu.carwash_server.payload.response.MessageResponse;
import ru.nsu.carwash_server.services.OperationsDescriptionService;
import ru.nsu.carwash_server.services.UserDetailsImpl;
import ru.nsu.carwash_server.services.interfaces.OperationService;
import ru.nsu.carwash_server.services.interfaces.OrderService;
import ru.nsu.carwash_server.services.interfaces.UserService;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Tag(name = "4. User Controller", description = "API для работы с профилем пользователя")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@RequestMapping("/api/user")
public class UserController {

    private final OperationService operationsService;

    private final OrderService orderService;

    private final UserService userService;

    private final PasswordEncoder encoder;

    private final OperationsDescriptionService operationsDescriptionService;

    @Autowired
    public UserController(
            OperationService operationsService,
            OrderService orderService,
            OperationsDescriptionService operationsDescriptionService,
            PasswordEncoder encoder,
            UserService userService) {
        this.operationsDescriptionService = operationsDescriptionService;
        this.operationsService = operationsService;
        this.encoder = encoder;
        this.userService = userService;
        this.orderService = orderService;
    }

    @Operation(
            summary = "Обновление пароля пользователя",
            description = "Этот метод позволяет пользователю обновить свой пароль, используя код подтверждения. Ограничение на количество попыток ввода кода."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пароль успешно обновлён.", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неверный код подтверждения.", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Превышено количество попыток ввода кода подтверждения.", content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    @Transactional
    @PostMapping("/updateUserPassword_v1")
    public ResponseEntity<MessageResponse> updateUserPassword(@Valid @RequestBody UpdateUserPasswordRequest updateUserPasswordRequest) {
        String userPhone = updateUserPasswordRequest.getPhone();

        if (operationsService.getAllDescriptionOperationsByTime(userPhone, "ввёл неверный код подтверждения для смены пароля", LocalDateTime.now().minusMinutes(30)).size() >= 4) {
            throw new TooManyAttemptsInCodeException();
        }
        if (!updateUserPasswordRequest.getSecretCode().equals(operationsService.getLatestCodeByPhoneNumber(userPhone) + "")) {
            String operationName = "User_write_wrong_code";
            String descriptionMessage = "Номер телефона:'" + userPhone + "' ввёл неверный код подтверждения для смены пароля";
            operationsService.SaveUserOperation(operationName, null, descriptionMessage, 1);
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Код подтверждения не совпадает!"));
        }

        var latestUserVersion = userService.getActualUserVersionByPhone(updateUserPasswordRequest.getPhone());

        String password = (encoder.encode(updateUserPasswordRequest.getPassword()));

        User user = latestUserVersion.getUser();

        UserVersions newVersion = new UserVersions(latestUserVersion, password, userPhone);
        user.addUserVersion(newVersion);

        String operationName = "Update_user_info_by_user";

        String descriptionMessage = "Пользователь " + updateUserPasswordRequest.getPhone() + " получил новый пароль";
        operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);

        log.info("updateUserPassword_v1. User with phone '{}' updated password.", updateUserPasswordRequest.getPhone());
        return ResponseEntity.ok(new MessageResponse("Пароль успешно обновлён!"));
    }

    @Operation(
            summary = "Обновление информации о пользователе",
            description = "Этот метод позволяет авторизованному пользователю обновить свои персональные данные."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Информация о пользователе успешно обновлена.", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Ошибка авторизации. Доступ запрещен из-за недействительного или отсутствующего JWT токена.", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден в базе данных.", content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    @Transactional
    @PostMapping("/updateUserInfo_v1")
    public ResponseEntity<MessageResponse> updateUserInfo(@Valid @RequestBody UpdateUserInfoRequest updateUserInfoRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();

        User user = userService.getFullUserById(userId);

        var latestUserVersion = userService.getActualUserVersionByPhone(updateUserInfoRequest.getPhone());
        Set<Role> roles;

        roles = latestUserVersion.getUser().getRoles();
        user.setRoles(roles);

        UserVersions newVersion = new UserVersions(latestUserVersion, updateUserInfoRequest);
        user.addUserVersion(newVersion);

        String operationName = "Update_user_info_by_user";

        String descriptionMessage = operationsDescriptionService.updateUserDescription(updateUserInfoRequest, newVersion.getPhone(), latestUserVersion);
        operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);
        log.info("updateUserInfo_v1. User with phone '{}' updated his profile.", updateUserInfoRequest.getPhone());

        return ResponseEntity.ok(new MessageResponse("Пользователь с айди '" + userId
                + "' и с телефоном " + updateUserInfoRequest.getPhone() + "изменил информацию о себе"));
    }


    @Operation(
            summary = "Получение заказов пользователя",
            description = "Этот метод возвращает историю заказов, сделанных текущим авторизованным пользователем, включая информацию о каждом заказе."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список заказов пользователя успешно получен.", content = @Content(schema = @Schema(implementation = OrderMainInfo[].class))),
            @ApiResponse(responseCode = "401", description = "Ошибка авторизации. Доступ запрещен из-за недействительного или отсутствующего JWT токена.", content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    @Transactional
    @GetMapping("/getUserOrders_v1")
    public ResponseEntity<List<OrderMainInfo>> getUserOrders() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();
        List<OrderMainInfo> userOrders = new ArrayList<>();
        User user = userService.getFullUserById(userId);

        for (var item : user.getOrders()) {
            Long orderId = item.getId();
            var latestOrderVersion = orderService.getActualOrderVersion(orderId);
            var allOrdersOfItem = getDividedOrders(latestOrderVersion);
            userOrders.add(new OrderMainInfo(orderId, latestOrderVersion.getOrderType(), item.getDateOfCreation(), latestOrderVersion.getStartTime(),
                    latestOrderVersion.getEndTime(), latestOrderVersion.getAutoNumber(), latestOrderVersion.getAutoType(), latestOrderVersion.getBonuses(),
                    latestOrderVersion.getPrice(), latestOrderVersion.getWheelR(), latestOrderVersion.getComments(),
                    allOrdersOfItem.getIncludedOrders(), allOrdersOfItem.getConnectedOrders(), latestOrderVersion.getCurrentStatus()));
        }

        return ResponseEntity.ok(userOrders);
    }


    public ConnectedOrdersResponse getDividedOrders(OrderVersions orderVersion) {
        ConnectedOrdersResponse allOrders = new ConnectedOrdersResponse(null, null);
        List<String> connectedOrders = new ArrayList<>();
        String orderType = orderVersion.getOrderType();
        if (orderType.contains("polishing")) {
            for (var item : orderVersion.getOrdersPolishings()) {
                connectedOrders.add(item.getName());
            }
            allOrders.setIncludedOrders(connectedOrders);
            return allOrders;
        } else if (orderType.contains("tire")) {
            for (var item : orderVersion.getOrdersTires()) {
                connectedOrders.add(item.getName());
            }
            allOrders.setIncludedOrders(connectedOrders);
            return allOrders;
        }
        var allWashingOrders = orderVersion.getOrdersWashing();
        for (var item : allWashingOrders) {
            connectedOrders.add(item.getName());
        }
        if (orderType.contains("ELITE")) {
            var mainELITEOrders = orderService.actualWashingOrders("ELITE").getIncludedOrders();
            connectedOrders.removeAll(mainELITEOrders);
            allOrders.setIncludedOrders(mainELITEOrders);
            allOrders.setConnectedOrders(connectedOrders);
            return allOrders;
        } else if (orderType.contains("VIP")) {
            var mainVIPOrders = orderService.actualWashingOrders("VIP").getIncludedOrders();
            connectedOrders.removeAll(mainVIPOrders);
            allOrders.setIncludedOrders(mainVIPOrders);
            allOrders.setConnectedOrders(connectedOrders);
            return allOrders;
        } else if (orderType.contains("Комфорт")) {
            var mainComfortOrders = orderService.actualWashingOrders("Комфорт").getIncludedOrders();
            connectedOrders.removeAll(mainComfortOrders);
            allOrders.setIncludedOrders(mainComfortOrders);
            allOrders.setConnectedOrders(connectedOrders);
            return allOrders;
        } else if (orderType.contains("Эконом")) {
            var mainEconomyOrders = orderService.actualWashingOrders("Эконом").getIncludedOrders();
            connectedOrders.removeAll(mainEconomyOrders);
            allOrders.setIncludedOrders(mainEconomyOrders);
            allOrders.setConnectedOrders(connectedOrders);
            return allOrders;
        } else if (orderType.contains("Стандарт")) {
            var mainStandardOrders = orderService.actualWashingOrders("Стандарт").getIncludedOrders();
            connectedOrders.removeAll(mainStandardOrders);
            allOrders.setIncludedOrders(mainStandardOrders);
            allOrders.setConnectedOrders(connectedOrders);
            return allOrders;
        } else {
            return allOrders;
        }
    }
}
