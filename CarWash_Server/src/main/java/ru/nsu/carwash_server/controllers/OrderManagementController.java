package ru.nsu.carwash_server.controllers;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.carwash_server.exceptions.NotInDataBaseException;
import ru.nsu.carwash_server.models.orders.OrderVersions;
import ru.nsu.carwash_server.models.orders.OrdersWashing;
import ru.nsu.carwash_server.models.secondary.helpers.SingleOrderResponse;
import ru.nsu.carwash_server.models.secondary.helpers.TimeAndPrice;
import ru.nsu.carwash_server.models.secondary.helpers.TimeIntervals;
import ru.nsu.carwash_server.models.secondary.helpers.TireOrderEntity;
import ru.nsu.carwash_server.models.secondary.helpers.WashingPolishingOrderEntity;
import ru.nsu.carwash_server.models.users.User;
import ru.nsu.carwash_server.models.users.UserVersions;
import ru.nsu.carwash_server.payload.request.FreeTimeRequest;
import ru.nsu.carwash_server.payload.request.OrdersArrayPriceAndGoodTimeRequest;
import ru.nsu.carwash_server.payload.request.OrdersArrayPriceTimeRequest;
import ru.nsu.carwash_server.payload.request.UpdateOrderInfoRequest;
import ru.nsu.carwash_server.payload.response.ActualOrdersResponse;
import ru.nsu.carwash_server.payload.response.AllOrdersEntity;
import ru.nsu.carwash_server.payload.response.ConnectedOrdersResponse;
import ru.nsu.carwash_server.payload.response.FreeTimeAndBoxResponse;
import ru.nsu.carwash_server.payload.response.MainAndAdditionalResponse;
import ru.nsu.carwash_server.payload.response.MessageResponse;
import ru.nsu.carwash_server.payload.response.OrdersArrayResponse;
import ru.nsu.carwash_server.payload.response.TimeAndPriceAndFreeTimeResponse;
import ru.nsu.carwash_server.payload.response.TimeAndPriceResponse;
import ru.nsu.carwash_server.payload.response.WashingOrdersPriceTimeAndPart;
import ru.nsu.carwash_server.repository.orders.OrdersPolishingRepository;
import ru.nsu.carwash_server.repository.orders.OrdersTireRepository;
import ru.nsu.carwash_server.repository.orders.OrdersWashingRepository;
import ru.nsu.carwash_server.services.UserDetailsImpl;
import ru.nsu.carwash_server.services.interfaces.OperationService;
import ru.nsu.carwash_server.services.interfaces.OrderService;
import ru.nsu.carwash_server.services.interfaces.UserService;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@RequestMapping("/api/orders/management")
@AllArgsConstructor
public class OrderManagementController {

    private final OrdersWashingRepository ordersWashingRepository;

    private final OrdersPolishingRepository ordersPolishingRepository;

    private final OrdersTireRepository ordersTireRepository;

    private final OrderService orderService;

    private final UserService userService;

    private final OperationService operationsService;

    @Autowired
    public OrderManagementController(
            OperationService operationsService,
            OrdersWashingRepository ordersWashingRepository,
            OrdersTireRepository ordersTireRepository,
            UserService userService,
            OrdersPolishingRepository ordersPolishingRepository,
            OrderService orderService) {
        this.ordersWashingRepository = ordersWashingRepository;
        this.ordersPolishingRepository = ordersPolishingRepository;
        this.ordersTireRepository = ordersTireRepository;
        this.userService = userService;
        this.orderService = orderService;
        this.operationsService = operationsService;
    }

    @PostMapping("/deleteOrder_v1")
    @Transactional
    public ResponseEntity<MessageResponse> deleteOrder(@Valid @RequestParam(name = "orderId") Long id) {
        var order = orderService.findById(id);
        if (order != null) {
            Pair<Boolean, String> result = orderService.deleteOrder(id);

            var booleanValue = result.getFirst();
            var resultText = result.getSecond();

            if (!booleanValue) {
                return ResponseEntity.badRequest().body(new MessageResponse(resultText));
            }

            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long userId = userDetails.getId();
            UserVersions userLatestVersion = userService.getActualUserVersionById(userId);

            String operationName = "Delete_order";
            String descriptionMessage = "Заказ с айди'" + id + "' отменён";
            operationsService.SaveUserOperation(operationName, userLatestVersion.getUser(), descriptionMessage, 1);

            log.info("deleteOrder_v1. User with phone '{}' cancelled order with id '{}'.", userLatestVersion.getPhone(), id);

            return ResponseEntity.ok(new MessageResponse("Заказ с айди " + id.toString() + " успешно удалён"));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Заказа с айди "
                    + id.toString() + " не существует"));
        }
    }

    @GetMapping("/getServiceInfo_v1")
    @Transactional
    public ResponseEntity<?> getServiceInfo(@Valid @RequestParam(name = "orderName") String orderName,
                                            @Valid @RequestParam(name = "orderType") String orderType) {
        switch (orderType) {
            case "Wash" -> {
                var order = ordersWashingRepository.findByName(orderName)
                        .orElseThrow(() -> new NotInDataBaseException("услуг мойки не найдена услуга: ", orderName.replace("_", " ")));
                return ResponseEntity.ok(order);
            }
            case "Polishing" -> {
                var order = ordersPolishingRepository.findByName(orderName)
                        .orElseThrow(() -> new NotInDataBaseException("услуг полировки не найдена услуга: ", orderName.replace("_", " ")));
                return ResponseEntity.ok(order);
            }
            case "Tire" -> {
                var order = ordersTireRepository.findByName(orderName)
                        .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", orderName.replace("_", " ")));
                return ResponseEntity.ok(order);
            }
        }
        return ResponseEntity.badRequest().body(new MessageResponse("Такая услуга не найдена в базе данных"));
    }

    @PostMapping("/updateOrderInfo_v1")
    @Transactional
    public ResponseEntity<MessageResponse> updateOrderInfo(@Valid @RequestBody UpdateOrderInfoRequest updateOrderInfoRequest) {
        Pair<Boolean, String> result = orderService.updateOrderInfo(updateOrderInfoRequest);
        var booleanValue = result.getFirst();
        var resultText = result.getSecond();

        if (!booleanValue) {
            return ResponseEntity.badRequest().body(new MessageResponse(resultText));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserVersions userLatestVersion = userService.getActualUserVersionById(userDetails.getId());

        User user = userLatestVersion.getUser();

        String operationName = "Update_order_info";

        String newWheelR = (updateOrderInfoRequest.getWheelR() != null) ?
                "новый размер шин: '" + updateOrderInfoRequest.getWheelR() + "', " : null;

        String newUserPhone = (updateOrderInfoRequest.getUserPhone() != null) ?
                "новый контакт клиента: '" + updateOrderInfoRequest.getUserPhone() + "', " : null;

        String newOrderType = (updateOrderInfoRequest.getOrderType() != null) ?
                "новый тип заказа: " + updateOrderInfoRequest.getOrderType() + "', " : null;

        String newPrice = (updateOrderInfoRequest.getPrice() != null) ?
                "новую цену: '" + updateOrderInfoRequest.getPrice() + "', " : null;

        String newStartTime = (updateOrderInfoRequest.getStartTime() != null) ?
                "новое время начала: '" + updateOrderInfoRequest.getStartTime() + "', " : null;

        String newEndTime = (updateOrderInfoRequest.getEndTime() != null) ?
                "новое время конца: '" + updateOrderInfoRequest.getEndTime() + "', " : null;

        String newAdministrator = (updateOrderInfoRequest.getAdministrator() != null) ?
                "нового администратора: '" + updateOrderInfoRequest.getAdministrator() + "', " : null;

        String newAutoNumber = (updateOrderInfoRequest.getAutoNumber() != null) ?
                "новый номер авто: '" + updateOrderInfoRequest.getAutoNumber() + "', " : null;

        String newAutoType = (updateOrderInfoRequest.getAutoType() != null) ?
                "новый тип авто: '" + updateOrderInfoRequest.getAutoType() + "', " : null;

        String newSpecialist = (updateOrderInfoRequest.getSpecialist() != null) ?
                "нового специалиста: '" + updateOrderInfoRequest.getSpecialist() + "', " : null;

        String newBoxNumber = (updateOrderInfoRequest.getBoxNumber() != null) ?
                "новый бокс: '" + updateOrderInfoRequest.getBoxNumber() + "', " : null;

        String newComments = (updateOrderInfoRequest.getComments() != null) ?
                "новые комментарии: '" + updateOrderInfoRequest.getComments() + "', " : null;

        String newOrders = (updateOrderInfoRequest.getOrders() != null) ?
                "новые услуги: '" + updateOrderInfoRequest.getOrders() + "', " : null;

        String newCurrentStatus = (updateOrderInfoRequest.getCurrentStatus() != null) ?
                "новое состояние: '" + updateOrderInfoRequest.getCurrentStatus() + "', " : null;


        String descriptionMessage = "Заказ с айди'" + updateOrderInfoRequest.getOrderId() + "' получил" +
                newWheelR + newUserPhone + newOrderType + newPrice + newStartTime + newEndTime +
                newAdministrator + newAutoType + newAutoNumber + newSpecialist + newBoxNumber +
                newBoxNumber + newComments + newOrders + newCurrentStatus;

        log.info("updateOrderInfo_v1. User with phone '{}' updated order with id '{}'.", userLatestVersion.getPhone(), updateOrderInfoRequest.getOrderId());

        operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);

        return ResponseEntity.ok(new MessageResponse(resultText));
    }

    @GetMapping("/getOrderInfo_v1")
    @Transactional
    public ResponseEntity<?> getOrderInfo(@Valid @RequestParam(name = "orderId", required = false) Long orderId) {
        if (orderId == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Несуществующий номер заказа"));
        }

        var orderById = orderService.findById(orderId);

        var actualOrder = orderService.getActualOrderVersion(orderId);

        List<String> ordersNames = new ArrayList<>();
        for (var washOrder : actualOrder.getOrdersWashing()) {
            ordersNames.add(washOrder.getName());
        }
        for (var polishingOrders : actualOrder.getOrdersPolishings()) {
            ordersNames.add(polishingOrders.getName());
        }
        for (var tireOrders : actualOrder.getOrdersTires()) {
            ordersNames.add(tireOrders.getName());
        }
        String userPhone;
        if (actualOrder.getUserContacts() != null) {
            userPhone = actualOrder.getUserContacts();
        } else {
            userPhone = userService.getActualUserVersionById(orderById.getUser().getId()).getPhone();
        }

        return ResponseEntity.ok(new SingleOrderResponse(orderId, orderById.getDateOfCreation(), actualOrder.getStartTime(),
                actualOrder.getEndTime(), actualOrder.getAdministrator(), actualOrder.getSpecialist(),
                actualOrder.getAutoNumber(), actualOrder.getAutoType(), actualOrder.getBoxNumber(), actualOrder.getBonuses(),
                actualOrder.getPrice(), actualOrder.getWheelR(), actualOrder.getComments(),
                ordersNames, userPhone, actualOrder.getOrderType(), actualOrder.getCurrentStatus(), actualOrder.getSale()));
    }

    @GetMapping("/getActualWashingOrders_v1")
    @Transactional
    public ResponseEntity<ConnectedOrdersResponse> getActualWashingServices(@Valid @RequestParam(name = "orderName") String orderName) {
        ConnectedOrdersResponse ordersInfo = orderService.actualWashingOrders(orderName);
        return ResponseEntity.ok(ordersInfo);
    }

    @GetMapping("/getAllWashingOrders_v1")
    @Transactional
    public ResponseEntity<MainAndAdditionalResponse> getAllWashingServices() {
        var mainOrders = orderService.getAllWashingOrdersByRole("main");
        var additionalOrders = orderService.getAllWashingOrdersByRole("additional");

        return ResponseEntity.ok(new MainAndAdditionalResponse(mainOrders, additionalOrders));
    }

    @GetMapping("/getAllWashingServicesWithPriceAndTime_v1")
    @Transactional
    public ResponseEntity<?> getAllWashingServicesWithPriceAndTime() {
        List<OrdersWashing> washingOrdersWithTimeAndPrice = orderService.findAllWashingService();
        if (washingOrdersWithTimeAndPrice.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Услуг мойки не существует"));
        }
        List<WashingOrdersPriceTimeAndPart> washingPolishingOrderEntities = new ArrayList<>();
        for (OrdersWashing services : washingOrdersWithTimeAndPrice) {
            washingPolishingOrderEntities.add(new WashingOrdersPriceTimeAndPart(services.getName().replace("_", " "),
                    services.getPriceFirstType(), services.getPriceSecondType(), services.getPriceThirdType(),
                    services.getTimeFirstType(), services.getTimeSecondType(), services.getTimeThirdType(), services.getRole(),
                    services.getAssociatedOrder()));
        }
        return ResponseEntity.ok(washingPolishingOrderEntities);
    }

    @GetMapping("/getAllPolishingServicesWithPriceAndTime_v1")
    @Transactional
    public ResponseEntity<?> getAllPolishingServicesWithPriceAndTime() {
        var polishingOrdersWithTimeAndPrice = orderService.findAllPolishingService();
        if (polishingOrdersWithTimeAndPrice.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Услуг полировки не существует"));
        }
        List<WashingPolishingOrderEntity> washingPolishingOrderEntities = new ArrayList<>();
        for (var services : polishingOrdersWithTimeAndPrice) {
            washingPolishingOrderEntities.add(new WashingPolishingOrderEntity(services.getName().replace("_", " "),
                    services.getPriceFirstType(), services.getPriceSecondType(), services.getPriceThirdType(),
                    services.getTimeFirstType(), services.getTimeSecondType(), services.getTimeThirdType(), null));
        }
        return ResponseEntity.ok(washingPolishingOrderEntities);
    }

    @GetMapping("/getAllServicesWithPriceAndTime_v1")
    @Transactional
    public ResponseEntity<?> getAllServicesWithPriceAndTime() {
        var washingOrdersWithTimeAndPrice = orderService.findAllWashingService();
        var polishingOrdersWithTimeAndPrice = orderService.findAllPolishingService();
        var tireServicesWithTimeAndPrice = orderService.findAllTireService();
        if (polishingOrdersWithTimeAndPrice.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Услуг полировки не существует"));
        }
        if (tireServicesWithTimeAndPrice.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Услуг шиномонтажа не существует"));
        }
        if (washingOrdersWithTimeAndPrice.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Услуг мойки не существует"));
        }
        List<AllOrdersEntity> allOrdersEntities = new ArrayList<>();
        for (var services : washingOrdersWithTimeAndPrice) {
            allOrdersEntities.add(new AllOrdersEntity(services.getName().replace("_", " "),
                    services.getPriceFirstType(), services.getPriceSecondType(), services.getPriceThirdType(),
                    services.getTimeFirstType(), services.getTimeSecondType(), services.getTimeThirdType(), null,
                    null, null, null, null, null, null, null,
                    null, null, null, null, null,
                    null, null, null, null, null, null, null,
                    "Washing"));
        }
        for (var services : polishingOrdersWithTimeAndPrice) {
            allOrdersEntities.add(new AllOrdersEntity(services.getName().replace("_", " "), services.getPriceFirstType(),
                    services.getPriceSecondType(), services.getPriceThirdType(), services.getTimeFirstType(), services.getTimeSecondType(),
                    services.getTimeThirdType(), null, null, null, null,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null,
                    null, "Polishing"));
        }
        for (var services : tireServicesWithTimeAndPrice) {
            allOrdersEntities.add(new AllOrdersEntity(services.getName().replace("_", " "),
                    null, null, null, null
                    , null, null, services.getPrice_r_13(), services.getPrice_r_14(),
                    services.getPrice_r_15(), services.getPrice_r_16(), services.getPrice_r_17(), services.getPrice_r_18(),
                    services.getPrice_r_19(), services.getPrice_r_20(), services.getPrice_r_21(), services.getPrice_r_22(),
                    services.getTime_r_13(), services.getTime_r_14(), services.getTime_r_15(), services.getTime_r_16(),
                    services.getTime_r_17(), services.getTime_r_18(), services.getTime_r_19(), services.getTime_r_20(),
                    services.getTime_r_21(), services.getTime_r_22(), "Tire"));
        }
        return ResponseEntity.ok(allOrdersEntities);
    }

    @GetMapping("/getAllTireServicesWithPriceAndTime_v1")
    @Transactional
    public ResponseEntity<?> getAllTireServicesWithPriceAndTime() {
        var tireServicesWithTimeAndPrice = orderService.findAllTireService();
        if (tireServicesWithTimeAndPrice.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Услуг шиномонтажа не существует"));
        }
        List<TireOrderEntity> tireOrderEntities = new ArrayList<>();
        for (var services : tireServicesWithTimeAndPrice) {
            tireOrderEntities.add(new TireOrderEntity(services.getName().replace("_", " "), services.getPrice_r_13(), services.getPrice_r_14(),
                    services.getPrice_r_15(), services.getPrice_r_16(), services.getPrice_r_17(), services.getPrice_r_18(),
                    services.getPrice_r_19(), services.getPrice_r_20(), services.getPrice_r_21(), services.getPrice_r_22(),
                    services.getTime_r_13(), services.getTime_r_14(), services.getTime_r_15(), services.getTime_r_16(),
                    services.getTime_r_17(), services.getTime_r_18(), services.getTime_r_19(), services.getTime_r_20(),
                    services.getTime_r_21(), services.getTime_r_22()));
        }
        return ResponseEntity.ok(tireOrderEntities);
    }

    @GetMapping("/getActualPolishingOrders_v1")
    @Transactional
    public ResponseEntity<ActualOrdersResponse> getActualPolishingOrders() {
        var orders = ordersPolishingRepository.getActualOrders()
                .orElse(null);
        return ResponseEntity.ok(new ActualOrdersResponse(orders));
    }

    @GetMapping("/getActualTireOrders_v1")
    @Transactional
    public ResponseEntity<ActualOrdersResponse> getActualTireOrders() {
        var orders = ordersTireRepository.getActualOrders()
                .orElse(null);
        return ResponseEntity.ok(new ActualOrdersResponse(orders));
    }


    @GetMapping("/getBookedTimeInOneDay_v1")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    @Transactional
    public ResponseEntity<OrdersArrayResponse> getBookedTimeInOneDay(@Valid @RequestParam(name = "startTime")
                                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startTime,
                                                                     @Valid @RequestParam(name = "endTime")
                                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endTime,
                                                                     @Valid @RequestParam(name = "includeCancelled", defaultValue = "false") Boolean includeCancelled) {
        List<OrderVersions> orders = orderService.getOrdersInTimeInterval(startTime,
                endTime, null, includeCancelled);


        List<SingleOrderResponse> ordersForResponse = getTimeAndPriceOfOrders(orders);
        return ResponseEntity.ok(new OrdersArrayResponse(ordersForResponse));
    }

    @GetMapping("/getNotMadeOrders_v1")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    @Transactional
    public ResponseEntity<OrdersArrayResponse> getNotMadeOrders(@RequestParam(name = "includeCancelled", defaultValue = "false") Boolean includeCancelled) {
        List<OrderVersions> orders = orderService.getNotMadeOrders(includeCancelled);
        List<SingleOrderResponse> ordersForResponse = getTimeAndPriceOfOrders(orders);
        return ResponseEntity.ok(new OrdersArrayResponse(ordersForResponse));
    }

    @GetMapping("/getOrderCreatedAt_v1")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    @Transactional
    public ResponseEntity<OrdersArrayResponse> getOrderCreatedAt(@Valid @RequestParam(name = "startTime")
                                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startTime,
                                                                 @Valid @RequestParam(name = "endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endTime,
                                                                 @Valid @RequestParam(name = "includeCancelled", defaultValue = "false") Boolean includeCancelled) {

        List<OrderVersions> orders = orderService.getOrdersCreatedAt(startTime,
                endTime, includeCancelled);
        List<SingleOrderResponse> ordersForResponse = getTimeAndPriceOfOrders(orders);

        return ResponseEntity.ok(new OrdersArrayResponse(ordersForResponse));
    }


    @PostMapping("/getPriceAndEndTime_v1")
    @Transactional
    public ResponseEntity<TimeAndPriceAndFreeTimeResponse> getPriceAndTime(@Valid @RequestBody OrdersArrayPriceAndGoodTimeRequest ordersArrayPriceTimeRequest) {
        List<TimeIntervals> timeIntervals = new ArrayList<>();
        int time = 0;
        int price = 0;
        TimeAndPrice timeAndPrice;
        switch (ordersArrayPriceTimeRequest.getOrderType()) {
            case "wash" -> {
                timeAndPrice = orderService.getWashingOrderPriceTime(ordersArrayPriceTimeRequest.getOrders(), ordersArrayPriceTimeRequest.getBodyType());
                price = timeAndPrice.getPrice();
                time = timeAndPrice.getTime();
            }
            case "tire" -> {
                timeAndPrice = orderService.getTireOrderTimePrice(ordersArrayPriceTimeRequest.getOrders(), ordersArrayPriceTimeRequest.getWheelR());
                price = timeAndPrice.getPrice();
                time = timeAndPrice.getTime();
            }
            case "polishing" -> {
                timeAndPrice = orderService.getPolishingOrderPriceAndTime(ordersArrayPriceTimeRequest.getOrders(), ordersArrayPriceTimeRequest.getBodyType());
                price = timeAndPrice.getPrice();
                time = timeAndPrice.getTime();
            }
        }

        Date startTimeFromRequest = ordersArrayPriceTimeRequest.getStartTime();
        if (time < 60) {
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 1, 22, 8, ordersArrayPriceTimeRequest.getOrderType()));
        } else if (time <= 120) {
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 2, 20, 8, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 2, 19, 9, ordersArrayPriceTimeRequest.getOrderType()));
        } else if (time <= 180) {
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 3, 20, 8, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 3, 19, 9, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 3, 18, 10, ordersArrayPriceTimeRequest.getOrderType()));
        } else if (time <= 240) {
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 4, 19, 8, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 4, 19, 9, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 4, 19, 10, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 4, 19, 11, ordersArrayPriceTimeRequest.getOrderType()));
        } else if (time <= 300) {
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 5, 17, 8, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 5, 17, 9, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 5, 17, 10, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 5, 17, 11, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 5, 17, 12, ordersArrayPriceTimeRequest.getOrderType()));
        } else if (time <= 361) {
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 6, 16, 8, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 6, 15, 9, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 6, 14, 10, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 6, 13, 11, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 6, 13, 12, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 6, 14, 13, ordersArrayPriceTimeRequest.getOrderType()));
        } else if (time <= 421) {
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 7, 17, 8, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 7, 17, 9, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 7, 17, 10, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 7, 17, 11, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 7, 17, 12, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 7, 17, 13, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 7, 17, 14, ordersArrayPriceTimeRequest.getOrderType()));
        } else {
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 8, 16, 8, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 8, 16, 9, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 8, 16, 10, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 8, 16, 11, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 8, 16, 12, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 8, 16, 13, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 8, 16, 14, ordersArrayPriceTimeRequest.getOrderType()));
        }

        List<OrderVersions> orders = orderService.getOrdersInTimeInterval(ordersArrayPriceTimeRequest.getStartTime(),
                ordersArrayPriceTimeRequest.getEndTime(), null, true);

        List<SingleOrderResponse> bookedOrders = getTimeAndPriceOfOrders(orders);

        List<TimeIntervals> clearOrdersWithoutDuplicates = getClearOrdersWithoutDuplicates(timeIntervals, bookedOrders);

        return ResponseEntity.ok(new TimeAndPriceAndFreeTimeResponse(price, time, clearOrdersWithoutDuplicates, new Date()));
    }

    @PostMapping("/getFreeTime_v1")
    @Transactional
    public ResponseEntity<FreeTimeAndBoxResponse> getFreeTime(@Valid @RequestBody FreeTimeRequest freeTimeRequest) {
        List<TimeIntervals> timeIntervals = new ArrayList<>();
        int time = freeTimeRequest.getOrderTime() + 15;

        Date startTimeFromRequest = freeTimeRequest.getStartTime();
        if (time < 60) {
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 1, 22, 8, freeTimeRequest.getOrderType()));
        } else if (time <= 120) {
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 2, 20, 8, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 2, 19, 9, freeTimeRequest.getOrderType()));
        } else if (time <= 180) {
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 3, 20, 8, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 3, 19, 9, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 3, 18, 10, freeTimeRequest.getOrderType()));
        } else if (time <= 240) {
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 4, 19, 8, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 4, 19, 9, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 4, 19, 10, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 4, 19, 11, freeTimeRequest.getOrderType()));
        } else if (time <= 300) {
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 5, 17, 8, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 5, 17, 9, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 5, 17, 10, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 5, 17, 11, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 5, 17, 12, freeTimeRequest.getOrderType()));
        } else if (time <= 361) {
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 6, 16, 8, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 6, 15, 9, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 6, 14, 10, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 6, 13, 11, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 6, 13, 12, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 6, 14, 13, freeTimeRequest.getOrderType()));
        } else if (time <= 421) {
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 7, 17, 8, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 7, 17, 9, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 7, 17, 10, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 7, 17, 11, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 7, 17, 12, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 7, 17, 13, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 7, 17, 14, freeTimeRequest.getOrderType()));
        } else {
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 8, 16, 8, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 8, 16, 9, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 8, 16, 10, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 8, 16, 11, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 8, 16, 12, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 8, 16, 13, freeTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 8, 16, 14, freeTimeRequest.getOrderType()));
        }

        List<OrderVersions> orders = orderService.getOrdersInTimeInterval(freeTimeRequest.getStartTime(),
                freeTimeRequest.getEndTime(), null, true);

        List<SingleOrderResponse> bookedOrders = getTimeAndPriceOfOrders(orders);

        List<TimeIntervals> clearOrdersWithoutDuplicates = getClearOrdersWithoutDuplicates(timeIntervals, bookedOrders);

        return ResponseEntity.ok(new FreeTimeAndBoxResponse(time, clearOrdersWithoutDuplicates, new Date()));
    }

    @GetMapping("/getPriceAndTime_v1")
    @Transactional
    public ResponseEntity<TimeAndPriceResponse> getPriceAndTime(@Valid @RequestBody OrdersArrayPriceTimeRequest ordersArrayPriceTimeRequest) {
        TimeAndPrice timeAndPrice = new TimeAndPrice(0, 0);
        switch (ordersArrayPriceTimeRequest.getOrderType()) {
            case "wash" ->
                    timeAndPrice = orderService.getWashingOrderPriceTime(ordersArrayPriceTimeRequest.getOrders(), ordersArrayPriceTimeRequest.getBodyType());
            case "tire" ->
                    timeAndPrice = orderService.getTireOrderTimePrice(ordersArrayPriceTimeRequest.getOrders(), ordersArrayPriceTimeRequest.getWheelR());
            case "polishing" ->
                    timeAndPrice = orderService.getPolishingOrderPriceAndTime(ordersArrayPriceTimeRequest.getOrders(), ordersArrayPriceTimeRequest.getBodyType());
        }
        return ResponseEntity.ok(new TimeAndPriceResponse(timeAndPrice.getPrice(), timeAndPrice.getTime()));
    }


    public List<TimeIntervals> fillTimeIntervals(Date startTimeFromRequest, int timeSkip, int endOfFor, int startOfFor, String orderType) {
        List<TimeIntervals> timeIntervals = new ArrayList<>();
        for (int i = startOfFor; i < endOfFor; i += timeSkip) {
            LocalDateTime localDateStartTime = LocalDateTime.ofInstant(startTimeFromRequest.toInstant(),
                            ZoneId.systemDefault())
                    .withHour(i)
                    .withMinute(0)
                    .withSecond(0);
            Date startTime = Date.from(localDateStartTime.atZone(ZoneId.systemDefault()).toInstant());

            LocalDateTime localDateEndTime = LocalDateTime.ofInstant(startTimeFromRequest.toInstant(),
                            ZoneId.systemDefault())
                    .withHour(i + timeSkip)
                    .withMinute(0)
                    .withSecond(0);
            Date endTime = Date.from(localDateEndTime.atZone(ZoneId.systemDefault()).toInstant());

            switch (orderType) {
                case "wash" -> {
                    TimeIntervals singleTimeIntervalFirstBox = new TimeIntervals(startTime, endTime, 1);
                    timeIntervals.add(singleTimeIntervalFirstBox);
                    TimeIntervals singleTimeIntervalSecondBox = new TimeIntervals(startTime, endTime, 2);
                    timeIntervals.add(singleTimeIntervalSecondBox);
                    TimeIntervals singleTimeIntervalThirdBox = new TimeIntervals(startTime, endTime, 3);
                    timeIntervals.add(singleTimeIntervalThirdBox);
                }
                case "tire" -> {
                    TimeIntervals singleTimeIntervalFirstBox = new TimeIntervals(startTime, endTime, 0);
                    timeIntervals.add(singleTimeIntervalFirstBox);
                }
                case "polishing" -> {
                    TimeIntervals singleTimeIntervalFirstBox = new TimeIntervals(startTime, endTime, 5);
                    timeIntervals.add(singleTimeIntervalFirstBox);
                }
            }
        }
        return timeIntervals;
    }


    private List<TimeIntervals> getClearOrdersWithoutDuplicates(List<TimeIntervals> timeIntervals, List<SingleOrderResponse> bookedOrders) {
        List<TimeIntervals> clearOrders = new ArrayList<>(timeIntervals);
        for (var bookedOrder : bookedOrders) {
            for (var freeTime : timeIntervals) {
                Date startTimeBooked = bookedOrder.getStartTime();
                Date endTimeBooked = bookedOrder.getEndTime();
                Date startTimeFree = freeTime.getStartTime();
                Date endTimeFree = freeTime.getEndTime();
                int freeBox = freeTime.getBox();
                int bookedBox = bookedOrder.getBoxNumber();
                if (((startTimeFree.compareTo(startTimeBooked) >= 0
                        && endTimeFree.compareTo(endTimeBooked) <= 0)
                        || (startTimeFree.compareTo(startTimeBooked) <= 0
                        && endTimeFree.compareTo(startTimeBooked) > 0)
                        || (startTimeFree.compareTo(endTimeBooked) < 0
                        && endTimeBooked.compareTo(endTimeFree) <= 0))
                        && freeBox == bookedBox) {
                    TimeIntervals timeIntervals1 = new TimeIntervals(startTimeFree, endTimeFree, freeBox);
                    clearOrders.remove(timeIntervals1);
                }
            }
        }

        List<TimeIntervals> noDuplicatesTimeList = new ArrayList<>(clearOrders);

        for (var firstInterval : clearOrders) {
            for (var secondInterval : clearOrders) {
                if (noDuplicatesTimeList.contains(secondInterval) && (firstInterval.getStartTime().equals(secondInterval.getStartTime())
                        && firstInterval.getEndTime().equals(secondInterval.getEndTime())
                        && !Objects.equals(firstInterval.getBox(), secondInterval.getBox()))
                        && noDuplicatesTimeList.contains(secondInterval) && noDuplicatesTimeList.contains(firstInterval)) {
                    noDuplicatesTimeList.remove(secondInterval);
                }
            }
        }
        return noDuplicatesTimeList;
    }


    public List<SingleOrderResponse> getTimeAndPriceOfOrders(List<OrderVersions> orders) {
        List<SingleOrderResponse> ordersForResponse = new ArrayList<>();

        for (var item : orders) {
            SingleOrderResponse newItem = null;

            String orderType = item.getOrderType();
            List<String> stringOrders = new ArrayList<>();
            String userContact;

            if (orderType.contains("polishing")) {
                for (var currentOrder : item.getOrdersPolishings()) {
                    stringOrders.add(currentOrder.getName().replace("_", " "));
                }
                userContact = item.getUserContacts();
                newItem = new SingleOrderResponse(item.getOrder().getId(), item.getDateOfCreation(), item.getStartTime()
                        , item.getEndTime(), item.getAdministrator(), item.getSpecialist(),
                        item.getAutoNumber(), item.getAutoType(), item.getBoxNumber(),
                        item.getBonuses(), item.getPrice(), item.getWheelR(),
                        item.getComments(), stringOrders, userContact,
                        item.getOrderType(), item.getCurrentStatus(), item.getSale());
            } else if (orderType.contains("tire")) {
                for (var currentOrder : item.getOrdersTires()) {
                    stringOrders.add(currentOrder.getName().replace("_", " "));
                }
                userContact = item.getUserContacts();
                newItem = new SingleOrderResponse(item.getOrder().getId(), item.getDateOfCreation(),
                        item.getStartTime(), item.getEndTime(), item.getAdministrator(),
                        item.getSpecialist(), item.getAutoNumber(),
                        item.getAutoType(), item.getBoxNumber(), item.getBonuses(),
                        item.getPrice(), item.getWheelR(), item.getComments(),
                        stringOrders, userContact, item.getOrderType(), item.getCurrentStatus(), item.getSale());
            } else if (orderType.contains("wash")) {
                for (var currentOrder : item.getOrdersWashing()) {
                    stringOrders.add(currentOrder.getName().replace("_", " "));
                }
                userContact = item.getUserContacts();

                newItem = new SingleOrderResponse(item.getOrder().getId(), item.getDateOfCreation(),
                        item.getStartTime(), item.getEndTime(), item.getAdministrator(),
                        item.getSpecialist(), item.getAutoNumber(), item.getAutoType(),
                        item.getBoxNumber(), item.getBonuses(), item.getPrice(), item.getWheelR(),
                        item.getComments(), stringOrders, userContact,
                        item.getOrderType(), item.getCurrentStatus(), item.getSale());
            }

            ordersForResponse.add(newItem);
        }

        return ordersForResponse;
    }
}