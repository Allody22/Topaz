package ru.nsu.carwash_server.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.carwash_server.models.orders.OrderVersions;
import ru.nsu.carwash_server.models.secondary.helpers.OrderMainInfo;
import ru.nsu.carwash_server.models.users.Role;
import ru.nsu.carwash_server.models.users.User;
import ru.nsu.carwash_server.models.users.UserVersions;
import ru.nsu.carwash_server.payload.request.UpdateUserInfoRequest;
import ru.nsu.carwash_server.payload.request.UpdateUserPasswordRequest;
import ru.nsu.carwash_server.payload.response.ConnectedOrdersResponse;
import ru.nsu.carwash_server.payload.response.MessageResponse;
import ru.nsu.carwash_server.repository.users.RoleRepository;
import ru.nsu.carwash_server.services.OperationsServiceIml;
import ru.nsu.carwash_server.services.UserDetailsImpl;
import ru.nsu.carwash_server.services.interfaces.OrderService;
import ru.nsu.carwash_server.services.interfaces.UserService;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@RequestMapping("/api/user")
public class UserController {

    private final OperationsServiceIml operationsService;

    private final OrderService orderService;

    private final UserService userService;

    private final RoleRepository roleRepository;

    @Autowired
    public UserController(
            OperationsServiceIml operationsService,
            OrderService orderService,
            UserService userService,
            RoleRepository roleRepository) {
        this.operationsService = operationsService;
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.orderService = orderService;
    }

    @Transactional
    @PostMapping("/updateUserPassword_v1")
    public ResponseEntity<?> updateUserPassword(@Valid @RequestBody UpdateUserPasswordRequest updateUserPasswordRequest) {

        if (!updateUserPasswordRequest.getSecretCode().equals(operationsService
                .getLatestCodeByPhoneNumber(updateUserPasswordRequest.getPhone()) + "")) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка: код подтверждения не совпадает!"));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();

        User user = userService.getFullUserById(userId);

        var latestUserVersion = userService.getActualUserVersionByPhone(updateUserPasswordRequest.getPhone());

        UserVersions newVersion = new UserVersions(latestUserVersion, updateUserPasswordRequest.getPassword(), updateUserPasswordRequest.getPhone());
        user.addUserVersion(newVersion);

        String operationName = "Update_user_info_by_user";

        String descriptionMessage = "Пользователь " + updateUserPasswordRequest.getPhone() + " получил новый пароль";
        operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);

        log.info("updateUserPassword_v1. User with phone '{}' updated password.", updateUserPasswordRequest.getPhone());
        return ResponseEntity.ok(new MessageResponse("Пользователь с айди '" + userId
                + "' с телефоном " + updateUserPasswordRequest.getPhone() + "обновил пароль или телефон"));
    }

    @Transactional
    @PostMapping("/updateUserInfo_v1")
    public ResponseEntity<?> updateUserInfo(@Valid @RequestBody UpdateUserInfoRequest updateUserInfoRequest) {
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

        String descriptionMessage = getString(updateUserInfoRequest, newVersion.getPhone());
        operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);
        log.info("updateUserInfo_v1. User with phone '{}' updated his profile.", updateUserInfoRequest.getPhone());

        return ResponseEntity.ok(new MessageResponse("Пользователь с айди '" + userId
                + "' и с телефоном " + updateUserInfoRequest.getPhone() + "изменил информацию о себе"));
    }

    @Transactional
    @GetMapping("/getUserOrders_v1")
    public ResponseEntity<?> getUserOrders() {
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

    private static String getString(UpdateUserInfoRequest updateUserInfoRequest, String username) {
        String newUsername = (updateUserInfoRequest.getPhone() != null) ?
                "новый username: '" + updateUserInfoRequest.getPhone() + "'," : null;

        String newFullName = (updateUserInfoRequest.getFullName() != null) ?
                "новое ФИО: '" + updateUserInfoRequest.getFullName() + "'," : null;

        String newAdminNote = (updateUserInfoRequest.getAdminNote() != null) ?
                "новую заметку от администратора: '" + updateUserInfoRequest.getAdminNote() + "'," : null;

        String newEmail = (updateUserInfoRequest.getEmail() != null) ?
                "новую почту: '" + updateUserInfoRequest.getEmail() + "'," : null;

        String newUserNote = (updateUserInfoRequest.getUserNote() != null) ?
                "новую заметку от самого пользователя: '" + updateUserInfoRequest.getUserNote() + "'," : null;

        return "Пользователь '" + username + "' получил" + newUsername +
                newFullName + newAdminNote + newEmail + newUserNote;
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
