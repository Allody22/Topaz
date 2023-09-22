package ru.nsu.carwash_server.controllers;


import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.carwash_server.models.orders.OrderVersions;
import ru.nsu.carwash_server.models.secondary.exception.NotInDataBaseException;
import ru.nsu.carwash_server.models.secondary.helpers.SingleOrderResponse;
import ru.nsu.carwash_server.models.secondary.helpers.TimeAndPrice;
import ru.nsu.carwash_server.models.secondary.helpers.TimeIntervals;
import ru.nsu.carwash_server.models.secondary.helpers.TireOrderEntity;
import ru.nsu.carwash_server.models.secondary.helpers.WashingPolishingOrderEntity;
import ru.nsu.carwash_server.models.users.User;
import ru.nsu.carwash_server.models.users.UserVersions;
import ru.nsu.carwash_server.payload.request.OrdersArrayPriceAndGoodTimeRequest;
import ru.nsu.carwash_server.payload.request.OrdersArrayPriceTimeRequest;
import ru.nsu.carwash_server.payload.request.UpdateOrderInfoRequest;
import ru.nsu.carwash_server.payload.response.ActualOrdersResponse;
import ru.nsu.carwash_server.payload.response.ConnectedOrdersResponse;
import ru.nsu.carwash_server.payload.response.MainAndAdditionalResponse;
import ru.nsu.carwash_server.payload.response.MessageResponse;
import ru.nsu.carwash_server.payload.response.OrdersArrayResponse;
import ru.nsu.carwash_server.payload.response.TimeAndPriceAndFreeTimeResponse;
import ru.nsu.carwash_server.payload.response.TimeAndPriceResponse;
import ru.nsu.carwash_server.repository.orders.OrdersPolishingRepository;
import ru.nsu.carwash_server.repository.orders.OrdersTireRepository;
import ru.nsu.carwash_server.repository.orders.OrdersWashingRepository;
import ru.nsu.carwash_server.services.OperationsService;
import ru.nsu.carwash_server.services.OrderServiceImp;
import ru.nsu.carwash_server.services.UserDetailsImpl;
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
@ControllerAdvice
@RequestMapping("/api/orders/management")
@AllArgsConstructor
public class OrderManagementController {

    private final OrdersWashingRepository ordersWashingRepository;

    private final OrdersPolishingRepository ordersPolishingRepository;

    private final OrdersTireRepository ordersTireRepository;

    private final OrderServiceImp orderServiceImp;

    private final UserService userService;

    private final OperationsService operationsService;

    @Autowired
    public OrderManagementController(
            OperationsService operationsService,
            OrdersWashingRepository ordersWashingRepository,
            OrdersTireRepository ordersTireRepository,
            UserService userService,
            OrdersPolishingRepository ordersPolishingRepository,
            OrderServiceImp orderServiceImp) {
        this.ordersWashingRepository = ordersWashingRepository;
        this.ordersPolishingRepository = ordersPolishingRepository;
        this.ordersTireRepository = ordersTireRepository;
        this.userService = userService;
        this.orderServiceImp = orderServiceImp;
        this.operationsService = operationsService;
    }

    @PostMapping("/deleteOrder_v1")
    @Transactional
    public ResponseEntity<?> deleteOrder(@RequestParam(name = "orderId", required = true) Long id) {
        var order = orderServiceImp.findById(id);
        if (order != null) {
            Pair<Boolean, String> result = orderServiceImp.deleteOrder(id);

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

            return ResponseEntity.ok(new MessageResponse("Заказ с айди " + id.toString() + " успешно удалён"));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Заказа с айди "
                    + id.toString() + " не существует"));
        }
    }

    @GetMapping("/getServiceInfo_v1")
    @Transactional
    public ResponseEntity<?> getServiceInfo(@RequestParam(name = "orderName", required = true) String orderName,
                                            @RequestParam(name = "orderType", required = true) String orderType) {
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
    public ResponseEntity<?> updateOrderInfo(@Valid @RequestBody UpdateOrderInfoRequest updateOrderInfoRequest) {
        Pair<Boolean, String> result = orderServiceImp.updateOrderInfo(updateOrderInfoRequest);
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
                "новый контакт клиента: '" + updateOrderInfoRequest.getUserPhone() + "', ": null;

        String newOrderType = (updateOrderInfoRequest.getOrderType() != null) ?
                "новый тип заказа: " + updateOrderInfoRequest.getOrderType() + "', ": null;

        String newPrice = (updateOrderInfoRequest.getPrice() != null) ?
                "новую цену: '" + updateOrderInfoRequest.getPrice() + "', ": null;

        String newStartTime = (updateOrderInfoRequest.getStartTime() != null) ?
                "новое время начала: '" + updateOrderInfoRequest.getStartTime() + "', " : null;

        String newEndTime = (updateOrderInfoRequest.getEndTime() != null) ?
                "новое время конца: '" + updateOrderInfoRequest.getEndTime() + "', ": null;

        String newAdministrator = (updateOrderInfoRequest.getAdministrator() != null) ?
                "нового администратора: '" + updateOrderInfoRequest.getAdministrator() + "', ": null;

        String newAutoNumber = (updateOrderInfoRequest.getAutoNumber() != null) ?
                "новый номер авто: '" + updateOrderInfoRequest.getAutoNumber() + "', ": null;

        String newAutoType = (updateOrderInfoRequest.getAutoType() != null) ?
                "новый тип авто: '" + updateOrderInfoRequest.getAutoType() + "', ": null;

        String newSpecialist = (updateOrderInfoRequest.getSpecialist() != null) ?
                "нового специалиста: '" + updateOrderInfoRequest.getSpecialist() + "', ": null;

        String newBoxNumber = (updateOrderInfoRequest.getBoxNumber() != null) ?
                "новый бокс: '" + updateOrderInfoRequest.getBoxNumber() + "', ": null;

        String newComments = (updateOrderInfoRequest.getComments() != null) ?
                "новые комментарии: '" + updateOrderInfoRequest.getComments() + "', ": null;

        String newOrders = (updateOrderInfoRequest.getOrders() != null) ?
                "новые услуги: '" + updateOrderInfoRequest.getOrders() + "', ": null;

        String newCurrentStatus = (updateOrderInfoRequest.getCurrentStatus() != null) ?
                "новое состояние: '" + updateOrderInfoRequest.getCurrentStatus() + "', ": null;


        String descriptionMessage = "Заказ с айди'" + updateOrderInfoRequest.getOrderId() + "' получил" +
                newWheelR + newUserPhone + newOrderType + newPrice + newStartTime + newEndTime +
                newAdministrator + newAutoType + newAutoNumber + newSpecialist + newBoxNumber +
                newBoxNumber + newComments + newOrders + newCurrentStatus;

        operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);

        return ResponseEntity.ok(new MessageResponse(resultText));
    }

    @GetMapping("/getOrderInfo_v1")
    @Transactional
    public ResponseEntity<?> getOrderInfo(@RequestParam(name = "orderId", required = false) Long orderId) {
        var orderById = orderServiceImp.findById(orderId);
        if (orderId == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Несуществующий номер заказа"));
        }

        var actualOrder = orderServiceImp.getActualOrderVersion(orderId);

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
        String userPhone = "Нет информации";
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
    public ResponseEntity<?> getActualWashingServices(@RequestParam(name = "orderName", required = true) String orderName) {
        ConnectedOrdersResponse ordersInfo = orderServiceImp.actualWashingOrders(orderName);
        return ResponseEntity.ok(ordersInfo);
    }

    @GetMapping("/getAllWashingOrders_v1")
    @Transactional
    public ResponseEntity<?> getAllWashingServices() {
        var mainOrders = orderServiceImp.getAllWashingOrdersByRole("main");
        var additionalOrders = orderServiceImp.getAllWashingOrdersByRole("additional");

        return ResponseEntity.ok(new MainAndAdditionalResponse(mainOrders, additionalOrders));
    }

    @GetMapping("/getAllWashingServicesWithPriceAndTime_v1")
    @Transactional
    public ResponseEntity<?> getAllWashingServicesWithPriceAndTime() {
        var washingOrdersWithTimeAndPrice = orderServiceImp.findAllWashingService();
        if (washingOrdersWithTimeAndPrice.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Услуг мойки не существует"));
        }
        List<WashingPolishingOrderEntity> washingPolishingOrderEntities = new ArrayList<>();
        for (var services : washingOrdersWithTimeAndPrice) {
            washingPolishingOrderEntities.add(new WashingPolishingOrderEntity(services.getName(),
                    services.getPriceFirstType(), services.getPriceSecondType(), services.getPriceThirdType(),
                    services.getTimeFirstType(), services.getTimeSecondType(), services.getTimeThirdType(), services.getRole()));
        }
        return ResponseEntity.ok(washingPolishingOrderEntities);
    }

    @GetMapping("/getAllPolishingServicesWithPriceAndTime_v1")
    @Transactional
    public ResponseEntity<?> getAllPolishingServicesWithPriceAndTime() {
        var washingOrdersWithTimeAndPrice = orderServiceImp.findAllPolishingService();
        if (washingOrdersWithTimeAndPrice.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Услуг полировки не существует"));
        }
        List<WashingPolishingOrderEntity> washingPolishingOrderEntities = new ArrayList<>();
        for (var services : washingOrdersWithTimeAndPrice) {
            washingPolishingOrderEntities.add(new WashingPolishingOrderEntity(services.getName(),
                    services.getPriceFirstType(), services.getPriceSecondType(), services.getPriceThirdType(),
                    services.getTimeFirstType(), services.getTimeSecondType(), services.getTimeThirdType(), null));
        }
        return ResponseEntity.ok(washingPolishingOrderEntities);
    }

    @GetMapping("/getAllTireServicesWithPriceAndTime_v1")
    @Transactional
    public ResponseEntity<?> getAllTireServicesWithPriceAndTime() {
        var tireServicesWithTimeAndPrice = orderServiceImp.findAllTireService();
        if (tireServicesWithTimeAndPrice.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Услуг шиномонтажа не существует"));
        }
        List<TireOrderEntity> tireOrderEntities = new ArrayList<>();
        for (var services : tireServicesWithTimeAndPrice) {
            tireOrderEntities.add(new TireOrderEntity(services.getName(), services.getPrice_r_13(), services.getPrice_r_14(),
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
    public ResponseEntity<?> getActualPolishingOrders() {
        var orders = ordersPolishingRepository.getActualOrders()
                .orElse(null);
        return ResponseEntity.ok(new ActualOrdersResponse(orders));
    }

    @GetMapping("/getActualTireOrders_v1")
    @Transactional
    public ResponseEntity<?> getActualTireOrders() {
        var orders = ordersTireRepository.getActualOrders()
                .orElse(null);
        return ResponseEntity.ok(new ActualOrdersResponse(orders));
    }


    @GetMapping("/getBookedTimeInOneDay_v1")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    @Transactional
    public ResponseEntity<?> getBookedTimeInOneDay(@RequestParam(name = "startTime")
                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startTime,
                                                   @RequestParam(name = "endTime")
                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endTime,
                                                   @RequestParam(name = "includeCancelled", defaultValue = "false") Boolean includeCancelled) {
        List<OrderVersions> orders = orderServiceImp.getOrdersInTimeInterval(startTime,
                endTime, null, includeCancelled);


        List<SingleOrderResponse> ordersForResponse = getTimeAndPriceOfOrders(orders);
        return ResponseEntity.ok(new OrdersArrayResponse(ordersForResponse));
    }

    @GetMapping("/getNotMadeOrders_v1")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    @Transactional
    public ResponseEntity<?> getNotMadeOrders(@RequestParam(name = "includeCancelled", defaultValue = "false") Boolean includeCancelled) {
        List<OrderVersions> orders = orderServiceImp.getNotMadeOrders(includeCancelled);
        List<SingleOrderResponse> ordersForResponse = getTimeAndPriceOfOrders(orders);
        return ResponseEntity.ok(new OrdersArrayResponse(ordersForResponse));
    }

    @GetMapping("/getOrderCreatedAt_v1")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    @Transactional
    public ResponseEntity<?> getOrderCreatedAt(@RequestParam(name = "startTime")
                                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startTime,
                                               @RequestParam(name = "endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endTime,
                                               @RequestParam(name = "includeCancelled", defaultValue = "false") Boolean includeCancelled) {

        List<OrderVersions> orders = orderServiceImp.getOrdersCreatedAt(startTime,
                endTime, includeCancelled);
        List<SingleOrderResponse> ordersForResponse = getTimeAndPriceOfOrders(orders);

        return ResponseEntity.ok(new OrdersArrayResponse(ordersForResponse));
    }


    @PostMapping("/getPriceAndEndTime_v1")
    @Transactional
    public ResponseEntity<?> getPriceAndTimeForSite(@Valid @RequestBody OrdersArrayPriceAndGoodTimeRequest ordersArrayPriceTimeRequest) {
        List<TimeIntervals> timeIntervals = new ArrayList<>();
        int time = 0;
        int price = 0;
        TimeAndPrice timeAndPrice;
        switch (ordersArrayPriceTimeRequest.getOrderType()) {
            case "wash" -> {
                timeAndPrice = washingOrderPriceTime(ordersArrayPriceTimeRequest.getOrders(), ordersArrayPriceTimeRequest.getBodyType());
                price = timeAndPrice.getPrice();
                time = timeAndPrice.getTime();
            }
            case "tire" -> {
                timeAndPrice = tireOrderTimePrice(ordersArrayPriceTimeRequest.getOrders(), ordersArrayPriceTimeRequest.getWheelR());
                price = timeAndPrice.getPrice();
                time = timeAndPrice.getTime();
            }
            case "polishing" -> {
                timeAndPrice = polishingOrderPriceTime(ordersArrayPriceTimeRequest.getOrders(), ordersArrayPriceTimeRequest.getBodyType());
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
        }  else {
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 8, 16, 8, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 8, 16, 9, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 8, 16, 10, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 8, 16, 11, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 8, 16, 12, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 8, 16, 13, ordersArrayPriceTimeRequest.getOrderType()));
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 8, 16, 14, ordersArrayPriceTimeRequest.getOrderType()));
        }

        List<OrderVersions> orders = orderServiceImp.getOrdersInTimeInterval(ordersArrayPriceTimeRequest.getStartTime(),
                ordersArrayPriceTimeRequest.getEndTime(), null, true);

        List<SingleOrderResponse> bookedOrders = getTimeAndPriceOfOrders(orders);

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

        Date currentDateTime = new Date();
        return ResponseEntity.ok(new TimeAndPriceAndFreeTimeResponse(price, time, noDuplicatesTimeList, currentDateTime));
    }

    @PostMapping("/getPriceAndTime_v1")
    @Transactional
    public ResponseEntity<?> getPriceAndTime(@Valid @RequestBody OrdersArrayPriceTimeRequest ordersArrayPriceTimeRequest) {
        TimeAndPrice timeAndPrice = new TimeAndPrice(0, 0);
        switch (ordersArrayPriceTimeRequest.getOrderType()) {
            case "wash" -> {
                timeAndPrice = washingOrderPriceTime(ordersArrayPriceTimeRequest.getOrders(), ordersArrayPriceTimeRequest.getBodyType());
            }
            case "tire" -> {
                timeAndPrice = tireOrderTimePrice(ordersArrayPriceTimeRequest.getOrders(), ordersArrayPriceTimeRequest.getWheelR());
            }
            case "polishing" -> {
                timeAndPrice = polishingOrderPriceTime(ordersArrayPriceTimeRequest.getOrders(), ordersArrayPriceTimeRequest.getBodyType());
            }
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


    public TimeAndPrice polishingOrderPriceTime(List<String> orderArray, int bodyType) {
        int price = 0;
        int time = 15;
        if (bodyType == 1) {
            for (var item : orderArray) {
                var currentOrder = ordersPolishingRepository.findByName(item.replace(" ", "_"))
                        .orElseThrow(() -> new NotInDataBaseException("услуг полировки не найдена услуга: ", item.replace("_", " ")));
                price += currentOrder.getPriceFirstType();
                time += currentOrder.getTimeFirstType();
            }
        } else if (bodyType == 2) {
            for (var item : orderArray) {
                var currentOrder = ordersPolishingRepository.findByName(item.replace(" ", "_"))
                        .orElseThrow(() -> new NotInDataBaseException("услуг полировки не найдена услуга: ", item.replace("_", " ")));
                price += currentOrder.getPriceSecondType();
                time += currentOrder.getTimeSecondType();
            }

        } else if (bodyType == 3) {
            for (var item : orderArray) {
                var currentOrder = ordersPolishingRepository.findByName(item.replace(" ", "_"))
                        .orElseThrow(() -> new NotInDataBaseException("услуг полировки не найдена услуга: ", item.replace("_", " ")));
                price += currentOrder.getPriceThirdType();
                time += currentOrder.getTimeThirdType();
            }
        }
        return new TimeAndPrice(time, price);
    }


    public TimeAndPrice washingOrderPriceTime(List<String> orderArray, int bodyType) {
        int price = 0;
        int time = 15;
        if (bodyType == 1) {
            for (var item : orderArray) {
                var currentOrder = ordersWashingRepository.findByName(item.replace(" ", "_"))
                        .orElseThrow(() -> new NotInDataBaseException("услуг мойки не найдена услуга: ", item.replace("_", " ")));
                price += currentOrder.getPriceFirstType();
                time += currentOrder.getTimeFirstType();
            }
        } else if (bodyType == 2) {
            for (var item : orderArray) {
                var currentOrder = ordersWashingRepository.findByName(item.replace(" ", "_"))
                        .orElseThrow(() -> new NotInDataBaseException("услуг мойки не найдена услуга: ", item.replace("_", " ")));
                price += currentOrder.getPriceSecondType();
                time += currentOrder.getTimeSecondType();
            }

        } else if (bodyType == 3) {
            for (var item : orderArray) {
                var currentOrder = ordersWashingRepository.findByName(item.replace(" ", "_"))
                        .orElseThrow(() -> new NotInDataBaseException("услуг мойки не найдена услуга: ", item.replace("_", " ")));
                price += currentOrder.getPriceThirdType();
                time += currentOrder.getTimeThirdType();
            }
        }
        return new TimeAndPrice(time, price);
    }

    public TimeAndPrice tireOrderTimePrice(List<String> orderArray, String wheelR) {
        int price = 0;
        int time = 15;
        switch (wheelR) {
            case "R13":
                for (var item : orderArray) {
                    var currentOrder = ordersTireRepository.findByName(item.replace(" ", "_"))
                            .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", item.replace("_", " ")));
                    price += currentOrder.getPrice_r_13();
                    time += currentOrder.getTime_r_13();
                }
                break;
            case "R14":
                for (var item : orderArray) {
                    var currentOrder = ordersTireRepository.findByName(item.replace(" ", "_"))
                            .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", item.replace("_", " ")));
                    price += currentOrder.getPrice_r_14();
                    time += currentOrder.getTime_r_14();
                }
                break;
            case "R15":
                for (var item : orderArray) {
                    var currentOrder = ordersTireRepository.findByName(item.replace(" ", "_"))
                            .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", item.replace("_", " ")));
                    price += currentOrder.getPrice_r_15();
                    time += currentOrder.getTime_r_15();
                }
                break;
            case "R16":
                for (var item : orderArray) {
                    var currentOrder = ordersTireRepository.findByName(item.replace(" ", "_"))
                            .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", item.replace("_", " ")));
                    price += currentOrder.getPrice_r_16();
                    time += currentOrder.getTime_r_16();
                }
                break;
            case "R17":
                for (var item : orderArray) {
                    var currentOrder = ordersTireRepository.findByName(item.replace(" ", "_"))
                            .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", item.replace("_", " ")));
                    price += currentOrder.getPrice_r_17();
                    time += currentOrder.getTime_r_17();
                }
                break;
            case "R18":
                for (var item : orderArray) {
                    var currentOrder = ordersTireRepository.findByName(item.replace(" ", "_"))
                            .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", item.replace("_", " ")));
                    price += currentOrder.getPrice_r_18();
                    time += currentOrder.getTime_r_18();
                }
                break;
            case "R19":
                for (var item : orderArray) {
                    var currentOrder = ordersTireRepository.findByName(item.replace(" ", "_"))
                            .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", item.replace("_", " ")));
                    price += currentOrder.getPrice_r_19();
                    time += currentOrder.getTime_r_19();
                }
                break;
            case "R20":
                for (var item : orderArray) {
                    var currentOrder = ordersTireRepository.findByName(item.replace(" ", "_"))
                            .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", item.replace("_", " ")));
                    price += currentOrder.getPrice_r_20();
                    time += currentOrder.getTime_r_20();
                }
                break;
            case "R21":
                for (var item : orderArray) {
                    var currentOrder = ordersTireRepository.findByName(item.replace(" ", "_"))
                            .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", item.replace("_", " ")));
                    price += currentOrder.getPrice_r_21();
                    time += currentOrder.getTime_r_21();
                }
                break;
            case "R22":
                for (var item : orderArray) {
                    var currentOrder = ordersTireRepository.findByName(item.replace(" ", "_"))
                            .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", item.replace("_", " ")));
                    price += currentOrder.getPrice_r_22();
                    time += currentOrder.getTime_r_22();
                }
                break;
        }
        return new TimeAndPrice(time, price);
    }
}