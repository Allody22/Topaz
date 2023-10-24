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
import ru.nsu.carwash_server.exceptions.EmptyOrdersArrayException;
import ru.nsu.carwash_server.exceptions.InvalidOrderTypeException;
import ru.nsu.carwash_server.exceptions.NotInDataBaseException;
import ru.nsu.carwash_server.models.orders.OrderVersions;
import ru.nsu.carwash_server.models.orders.OrdersWashing;
import ru.nsu.carwash_server.models.secondary.helpers.SingleOrderResponse;
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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
    public ResponseEntity<MessageResponse> deleteOrder(@Valid @NotNull @RequestParam(name = "orderId") Long orderId) {
        orderService.deleteOrder(orderId);
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();
        UserVersions userLatestVersion = userService.getActualUserVersionById(userId);

        String operationName = "Delete_order";
        String descriptionMessage = "Заказ с айди'" + orderId + "' отменён";
        operationsService.SaveUserOperation(operationName, userLatestVersion.getUser(), descriptionMessage, 1);

        log.info("deleteOrder_v1. User with phone '{}' cancelled order with id '{}'.", userLatestVersion.getPhone(), orderId);

        return ResponseEntity.ok(new MessageResponse("Заказ с айди " + orderId.toString() + " успешно удалён"));
    }

    @GetMapping("/getServiceInfo_v1")
    @Transactional
    public ResponseEntity<?> getServiceInfo(@Valid @NotBlank @RequestParam(name = "orderName") String orderName,
                                            @Valid @NotBlank @RequestParam(name = "orderType") String orderType) {
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
            default -> throw new InvalidOrderTypeException(orderType);
        }
    }

    @PostMapping("/updateOrderInfo_v1")
    @Transactional
    public ResponseEntity<MessageResponse> updateOrderInfo(@Valid @RequestBody UpdateOrderInfoRequest updateOrderInfoRequest) {
        String descriptionMessage = orderService.updateOrderInfo(updateOrderInfoRequest);

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserVersions userLatestVersion = userService.getActualUserVersionById(userDetails.getId());

        User user = userLatestVersion.getUser();

        String operationName = "Update_order_info";

        log.info("updateOrderInfo_v1. User with phone '{}' updated order with id '{}'.", userLatestVersion.getPhone(), updateOrderInfoRequest.getOrderId());

        operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);

        return ResponseEntity.ok(new MessageResponse("Информация о заказе успешно обновлена"));
    }

    @GetMapping("/getOrderInfo_v1")
    @Transactional
    public ResponseEntity<?> getOrderInfo(@Valid @NotNull @RequestParam(name = "orderId", required = true) Long orderId) {

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
    public ResponseEntity<List<WashingOrdersPriceTimeAndPart>> getAllWashingServicesWithPriceAndTime() {
        List<OrdersWashing> washingOrdersWithTimeAndPrice = orderService.findAllWashingService();
        if (washingOrdersWithTimeAndPrice.isEmpty()) {
            throw new NotInDataBaseException("услуг мойки не найдено ", "никаких услуг мойки");
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
    public ResponseEntity<List<WashingPolishingOrderEntity>> getAllPolishingServicesWithPriceAndTime() {
        var polishingOrdersWithTimeAndPrice = orderService.findAllPolishingService();
        if (polishingOrdersWithTimeAndPrice.isEmpty()) {
            throw new NotInDataBaseException("услуг полировки не найдено ", "никаких услуг полировки");
        }
        List<WashingPolishingOrderEntity> washingPolishingOrderEntities = new ArrayList<>();
        for (var services : polishingOrdersWithTimeAndPrice) {
            washingPolishingOrderEntities.add(new WashingPolishingOrderEntity(services.getName().replace("_", " "),
                    services.getPriceFirstType(), services.getPriceSecondType(), services.getPriceThirdType(),
                    services.getTimeFirstType(), services.getTimeSecondType(), services.getTimeThirdType(), null));
        }
        return ResponseEntity.ok(washingPolishingOrderEntities);
    }


    @GetMapping("/getAllTireServicesWithPriceAndTime_v1")
    @Transactional
    public ResponseEntity<?> getAllTireServicesWithPriceAndTime() {
        var tireServicesWithTimeAndPrice = orderService.findAllTireService();
        if (tireServicesWithTimeAndPrice.isEmpty()) {
            throw new NotInDataBaseException("услуг шиномонтажа не найдено ", "никаких услуг шиномонтажа");
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

    @GetMapping("/getAllServicesWithPriceAndTime_v1")
    @Transactional
    public ResponseEntity<?> getAllServicesWithPriceAndTime() {
        var washingOrdersWithTimeAndPrice = orderService.findAllWashingService();
        var polishingOrdersWithTimeAndPrice = orderService.findAllPolishingService();
        var tireServicesWithTimeAndPrice = orderService.findAllTireService();
        if (polishingOrdersWithTimeAndPrice.isEmpty()) {
            throw new NotInDataBaseException("услуг полировки не найдено ", "никаких услуг полировки");
        }
        if (tireServicesWithTimeAndPrice.isEmpty()) {
            throw new NotInDataBaseException("услуг шиномонтажа не найдено ", "никаких услуг шиномонтажа");
        }
        if (washingOrdersWithTimeAndPrice.isEmpty()) {
            throw new NotInDataBaseException("услуг мойки не найдено ", "никаких услуг мойки");
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
    public ResponseEntity<OrdersArrayResponse> getBookedTimeInOneDay(@Valid @NotNull @RequestParam(name = "startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startTime,
                                                                     @Valid @NotNull @RequestParam(name = "endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endTime,
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
    public ResponseEntity<OrdersArrayResponse> getOrderCreatedAt(@Valid @NotNull @RequestParam(name = "startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startTime,
                                                                 @Valid @NotNull @RequestParam(name = "endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endTime,
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
        Pair<Integer, Integer> timeAndPrice = Pair.of(0, 0);
        switch (ordersArrayPriceTimeRequest.getOrderType()) {
            case "wash" ->
                    timeAndPrice = orderService.getWashingOrderPriceTime(ordersArrayPriceTimeRequest.getOrders(), ordersArrayPriceTimeRequest.getBodyType());
            case "tire" ->
                    timeAndPrice = orderService.getTireOrderTimePrice(ordersArrayPriceTimeRequest.getOrders(), ordersArrayPriceTimeRequest.getWheelR());
            case "polishing" ->
                    timeAndPrice = orderService.getPolishingOrderPriceAndTime(ordersArrayPriceTimeRequest.getOrders(), ordersArrayPriceTimeRequest.getBodyType());
            default -> throw new InvalidOrderTypeException(ordersArrayPriceTimeRequest.getOrderType());
        }

        int orderTime = timeAndPrice.getFirst();

        if (orderTime == 0) {
            throw new EmptyOrdersArrayException();
        }
        Date startTimeFromRequest = ordersArrayPriceTimeRequest.getStartTime();

        if (orderTime >= 820) {
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 28, 22, 8, ordersArrayPriceTimeRequest.getOrderType()));
        } else {
            int intervalsNeeded = (int) Math.ceil(orderTime / 30.0);
            int startHour = 8;
            int endHour = 22;
            for (int i = 0; i < intervalsNeeded; i++) {
                if (startHour < 8 || startHour > 22 || endHour < 8 || endHour > 22) {
                    continue;
                }
                timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, intervalsNeeded, endHour, startHour, ordersArrayPriceTimeRequest.getOrderType()));
                startHour++;
                endHour--;

            }
        }

        List<OrderVersions> orders = orderService.getOrdersInTimeInterval(ordersArrayPriceTimeRequest.getStartTime(),
                ordersArrayPriceTimeRequest.getEndTime(), null, true);

        List<SingleOrderResponse> bookedOrders = getTimeAndPriceOfOrders(orders);

        List<TimeIntervals> clearOrdersWithoutDuplicates = getClearOrdersWithoutDuplicates(timeIntervals, bookedOrders);

        return ResponseEntity.ok(new TimeAndPriceAndFreeTimeResponse(timeAndPrice.getSecond(), timeAndPrice.getFirst(), clearOrdersWithoutDuplicates, new Date()));
    }

    @PostMapping("/getFreeTime_v1")
    @Transactional
    public ResponseEntity<FreeTimeAndBoxResponse> getFreeTime(@Valid @RequestBody FreeTimeRequest freeTimeRequest) {
        int time = freeTimeRequest.getOrderTime();
        if (time == 0) {
            throw new EmptyOrdersArrayException();
        }

        List<TimeIntervals> timeIntervals = new ArrayList<>();
        time += 15;

        Date startTimeFromRequest = freeTimeRequest.getStartTime();
        if (time >= 820) {
            timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, 28, 22, 8, freeTimeRequest.getOrderType()));
        } else {
            int intervalsNeeded = (int) Math.ceil(time / 30.0);
            int startHour = 8;
            int endHour = 22;
            for (int i = 0; i < intervalsNeeded; i++) {
                if (startHour < 8 || startHour > 22 || endHour < 8 || endHour > 22) {
                    continue;
                }
                timeIntervals.addAll(fillTimeIntervals(startTimeFromRequest, intervalsNeeded, endHour, startHour, freeTimeRequest.getOrderType()));
                startHour++;
                endHour--;

            }
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
        Pair<Integer, Integer> timeAndPrice = Pair.of(0, 0);
        switch (ordersArrayPriceTimeRequest.getOrderType()) {
            case "wash" ->
                    timeAndPrice = orderService.getWashingOrderPriceTime(ordersArrayPriceTimeRequest.getOrders(), ordersArrayPriceTimeRequest.getBodyType());
            case "tire" ->
                    timeAndPrice = orderService.getTireOrderTimePrice(ordersArrayPriceTimeRequest.getOrders(), ordersArrayPriceTimeRequest.getWheelR());
            case "polishing" ->
                    timeAndPrice = orderService.getPolishingOrderPriceAndTime(ordersArrayPriceTimeRequest.getOrders(), ordersArrayPriceTimeRequest.getBodyType());
            default -> throw new InvalidOrderTypeException(ordersArrayPriceTimeRequest.getOrderType());
        }
        if (timeAndPrice.getFirst() == 0 || timeAndPrice.getSecond() == 0) {
            throw new EmptyOrdersArrayException();
        }
        return ResponseEntity.ok(new TimeAndPriceResponse(timeAndPrice.getSecond(), timeAndPrice.getFirst()));
    }


    public List<TimeIntervals> fillTimeIntervals(Date startTimeFromRequest, int timeSkip, int endOfFor, int startOfFor, String orderType) {
        List<TimeIntervals> timeIntervals = new ArrayList<>();
        for (int i = startOfFor * 2; i < endOfFor * 2; i += timeSkip) {
            int hour = i / 2;
            int minute = (i % 2) * 30;

            LocalDateTime localDateStartTime = LocalDateTime.ofInstant(startTimeFromRequest.toInstant(),
                            ZoneId.systemDefault())
                    .withHour(hour)
                    .withMinute(minute)
                    .withSecond(0);
            Date startTime = Date.from(localDateStartTime.atZone(ZoneId.systemDefault()).toInstant());

            if (localDateStartTime.getHour() < 8 || localDateStartTime.getHour() >= 22) {
                continue;
            }

            LocalDateTime localDateEndTime = localDateStartTime.plusMinutes(30L * timeSkip);
            Date endTime = Date.from(localDateEndTime.atZone(ZoneId.systemDefault()).toInstant());

            if (localDateEndTime.getHour() < 8 || localDateEndTime.getHour() > 22) {
                continue;
            }

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
                default -> throw new InvalidOrderTypeException(orderType);
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