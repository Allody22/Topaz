package ru.nsu.carwash_server.controllers;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.carwash_server.exceptions.BadBoxException;
import ru.nsu.carwash_server.exceptions.NotInDataBaseException;
import ru.nsu.carwash_server.exceptions.TimeSlotUnavailableException;
import ru.nsu.carwash_server.exceptions.TooManyOrdersException;
import ru.nsu.carwash_server.models.orders.Order;
import ru.nsu.carwash_server.models.orders.OrderVersions;
import ru.nsu.carwash_server.models.orders.OrdersPolishing;
import ru.nsu.carwash_server.models.orders.OrdersTire;
import ru.nsu.carwash_server.models.orders.OrdersWashing;
import ru.nsu.carwash_server.models.secondary.constants.DestinationPrefixes;
import ru.nsu.carwash_server.models.secondary.constants.OrderTypes;
import ru.nsu.carwash_server.models.users.User;
import ru.nsu.carwash_server.models.users.UserVersions;
import ru.nsu.carwash_server.payload.request.BookingTireOrderRequest;
import ru.nsu.carwash_server.payload.request.BookingWashingPolishingOrderRequest;
import ru.nsu.carwash_server.payload.request.CreatingPolishingOrder;
import ru.nsu.carwash_server.payload.request.CreatingTireOrderRequest;
import ru.nsu.carwash_server.payload.request.CreatingWashingOrder;
import ru.nsu.carwash_server.payload.response.OrderInfoResponse;
import ru.nsu.carwash_server.repository.orders.OrdersPolishingRepository;
import ru.nsu.carwash_server.repository.orders.OrdersTireRepository;
import ru.nsu.carwash_server.repository.orders.OrdersWashingRepository;
import ru.nsu.carwash_server.services.UserDetailsImpl;
import ru.nsu.carwash_server.services.interfaces.OperationService;
import ru.nsu.carwash_server.services.interfaces.OrderService;
import ru.nsu.carwash_server.services.interfaces.UserService;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@ControllerAdvice
@Slf4j
@RequestMapping("/api/orders/new")
public class OrderCreatingController {

    private final OrdersWashingRepository ordersWashingRepository;

    private final OrdersPolishingRepository ordersPolishingRepository;

    private final OrdersTireRepository ordersTireRepository;

    private final OrderService orderService;

    private SimpMessagingTemplate simpMessagingTemplate;

    private final OperationService operationsService;

    private final UserService userService;

    @Autowired
    public OrderCreatingController(
            OrdersWashingRepository ordersWashingRepository,
            OrdersTireRepository ordersTireRepository,
            UserService userService,
            OrdersPolishingRepository ordersPolishingRepository,
            OrderService orderService,
            OperationService operationsService,
            SimpMessagingTemplate simpMessagingTemplate) {
        this.userService = userService;
        this.operationsService = operationsService;
        this.ordersWashingRepository = ordersWashingRepository;
        this.ordersPolishingRepository = ordersPolishingRepository;
        this.ordersTireRepository = ordersTireRepository;
        this.orderService = orderService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }


    @Transactional
    @PostMapping("/bookWashingOrder_v1")
    @SendTo(DestinationPrefixes.NOTIFICATIONS)
    public ResponseEntity<?> newWashingOrder(@Valid @RequestBody BookingWashingPolishingOrderRequest bookingOrderRequest) {
        int boxNumber = bookingOrderRequest.getBoxNumber();
        if (boxNumber < 1 || boxNumber > 3) {
            throw new BadBoxException(boxNumber, "мойка");
        }

        Date startTime = bookingOrderRequest.getStartTime();
        Date endTime = bookingOrderRequest.getEndTime();

        if (!orderService.checkIfTimeFree(startTime, endTime, boxNumber)) {
            throw new TimeSlotUnavailableException(boxNumber);
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();
        User user = userService.getFullUserById(userId);
        UserVersions lastUserVersion = userService.getActualUserVersionById(userId);

        if (!(lastUserVersion.getPhone().equals("79635186660"))) {
            ZonedDateTime startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

            Date startOfTheDay = Date.from(startOfDay.toInstant());
            Date endOfTheDay = Date.from(endOfDay.toInstant());

            List<Order> userTodayOrders = orderService.getUserOrdersInTimeInterval(startOfTheDay, endOfTheDay, userId);
            if (userTodayOrders.size() >= 2) {
                throw new TooManyOrdersException();
            }
        }

        List<String> ordersList = bookingOrderRequest.getOrders();
        List<OrdersWashing> ordersWashings = new ArrayList<>();

        for (var order : ordersList) {
            var currentOrder = ordersWashingRepository.findByName(order.replace(" ", "_"))
                    .orElseThrow(() -> new NotInDataBaseException("услуг мойки не найдена услуга: ", order));
            ordersWashings.add(currentOrder);
        }

        String bookingOrderRequestOrderType = bookingOrderRequest.getOrderType();
        Integer price = bookingOrderRequest.getPrice();
        int autoType = bookingOrderRequest.getAutoType();
        String comments = bookingOrderRequest.getComments();
        String autoNumber = bookingOrderRequest.getAutoNumber();
        String sale = bookingOrderRequest.getSale();
        String administrator = bookingOrderRequest.getAdministrator();
        String specialist = bookingOrderRequest.getSpecialist();
        int bonuses = bookingOrderRequest.getBonuses();
        String currentStatus = bookingOrderRequest.getCurrentStatus();
        String userPhone = lastUserVersion.getPhone();

        if (lastUserVersion.getFullName() != null && !lastUserVersion.getFullName().isBlank()) {
            userPhone += "; " + lastUserVersion.getFullName();
        }

        if (price == null || price == 0) {
            price = orderService.getWashingOrderPriceTime(ordersList, autoType).getSecond();
        }

        String orderType = "wash " + bookingOrderRequestOrderType;

        Pair<Order, OrderVersions> result = orderService.saveWashingOrder(
                ordersWashings, startTime, endTime, administrator, specialist,
                boxNumber, bonuses, comments, autoNumber, autoType,
                userPhone, user, price, orderType,
                currentStatus, 1, sale);
        Order newOrder = result.getFirst();
        OrderVersions orderCurrentVersions = result.getSecond();

        simpMessagingTemplate.convertAndSend(orderCurrentVersions);

        String operationName = "Book_washing_order";
        String descriptionMessage = "Пользователь с логином '" + userPhone + "' забронировал заказ на автомойку";

        operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);

        log.info("bookWashingOrder_v1. User with phone '{}' booked a car wash order.", userPhone);

        return ResponseEntity.ok(new OrderInfoResponse(newOrder.getId(), ordersList,
                startTime, endTime, administrator, specialist, boxNumber, autoNumber,
                autoType, bonuses, comments, newOrder.getUser().getId(), price, orderType, "wash", orderCurrentVersions.getCurrentStatus()));
    }

    @Transactional
    @PostMapping("/bookPolishingOrder_v1")
    public ResponseEntity<?> newPolishingOrder(@Valid @RequestBody BookingWashingPolishingOrderRequest bookingOrderRequest) {
        int boxNumber = bookingOrderRequest.getBoxNumber();
        if (boxNumber != 5) {
            throw new BadBoxException(boxNumber, "полировка");
        }

        Date startTime = bookingOrderRequest.getStartTime();
        Date endTime = bookingOrderRequest.getEndTime();

        if (!orderService.checkIfTimeFree(startTime, endTime, boxNumber)) {
            throw new TimeSlotUnavailableException(boxNumber);
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();

        User user = userService.getFullUserById(userId);
        UserVersions lastUserVersion = userService.getActualUserVersionById(userId);

        if (!(lastUserVersion.getPhone().equals("79635186660"))) {
            ZonedDateTime startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

            Date startOfTheDay = Date.from(startOfDay.toInstant());
            Date endOfTheDay = Date.from(endOfDay.toInstant());

            List<Order> userTodayOrders = orderService.getUserOrdersInTimeInterval(startOfTheDay, endOfTheDay, userId);
            if (userTodayOrders.size() >= 2) {
                throw new TooManyOrdersException();
            }
        }

        List<String> ordersList = bookingOrderRequest.getOrders();
        List<OrdersPolishing> ordersPolishings = new ArrayList<>();

        for (var order : ordersList) {
            var currentOrder = ordersPolishingRepository.findByName(order.replace(" ", "_"))
                    .orElseThrow(() -> new NotInDataBaseException("услуг полировки не найдена услуга: ", order.replace("_", " ")));
            ordersPolishings.add(currentOrder);
        }

        Integer price = bookingOrderRequest.getPrice();
        int autoType = bookingOrderRequest.getAutoType();

        if (price == null || price == 0) {
            price = orderService.getPolishingOrderPriceAndTime(ordersList, autoType).getSecond();
        }

        String bookingOrderRequestOrderType = bookingOrderRequest.getOrderType();
        String comments = bookingOrderRequest.getComments();
        String autoNumber = bookingOrderRequest.getAutoNumber();
        String sale = bookingOrderRequest.getSale();
        String administrator = bookingOrderRequest.getAdministrator();
        String specialist = bookingOrderRequest.getSpecialist();
        int bonuses = bookingOrderRequest.getBonuses();
        String currentStatus = bookingOrderRequest.getCurrentStatus();
        String userPhone = lastUserVersion.getPhone();

        if (lastUserVersion.getFullName() != null && !lastUserVersion.getFullName().isBlank()) {
            userPhone += "; " + lastUserVersion.getFullName();
        }

        Pair<Order, OrderVersions> result = orderService.savePolishingOrder(ordersPolishings, startTime,
                endTime, administrator, specialist, boxNumber, bonuses, comments,
                autoNumber, autoType, userPhone, user, price,
                OrderTypes.polishingApp, currentStatus, 1, sale);

        Order newOrder = result.getFirst();
        OrderVersions orderCurrentVersions = result.getSecond();

        simpMessagingTemplate.convertAndSend(orderCurrentVersions);

        String operationName = "Book_polishing_order";
        String descriptionMessage = "Пользователь с логином '" + lastUserVersion.getPhone() + "' забронировал заказ на полировку";

        operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);

        log.info("bookPolishingOrder_v1. User with phone '{}' booked a car wash order.", userPhone);

        return ResponseEntity.ok(new OrderInfoResponse(newOrder.getId(), ordersList,
                startTime, endTime, administrator, specialist, boxNumber, autoNumber,
                autoType, bonuses, comments, newOrder.getUser().getId(), price,
                bookingOrderRequestOrderType, "polishing", currentStatus));
    }

    @Transactional
    @PostMapping("/bookTireOrder_v1")
    public ResponseEntity<?> newTireOrder(@Valid @RequestBody BookingTireOrderRequest bookingOrderRequest) {
        int boxNumber = bookingOrderRequest.getBoxNumber();
        if (boxNumber != 0) {
            throw new BadBoxException(boxNumber, "шиномонтаж");
        }

        Date startTime = bookingOrderRequest.getStartTime();
        Date endTime = bookingOrderRequest.getEndTime();

        if (!orderService.checkIfTimeFree(startTime, endTime, boxNumber)) {
            throw new TimeSlotUnavailableException(boxNumber);
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();

        User user = userService.getFullUserById(userId);
        UserVersions lastUserVersion = userService.getActualUserVersionById(userId);

        if (!(lastUserVersion.getPhone().equals("79635186660"))) {
            ZonedDateTime startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

            Date startOfTheDay = Date.from(startOfDay.toInstant());
            Date endOfTheDay = Date.from(endOfDay.toInstant());

            List<Order> userTodayOrders = orderService.getUserOrdersInTimeInterval(startOfTheDay, endOfTheDay, userId);
            if (userTodayOrders.size() >= 2) {
                throw new TooManyOrdersException();
            }
        }
        List<OrdersTire> ordersTireService = new ArrayList<>();
        List<String> ordersList = bookingOrderRequest.getOrders();

        for (var order : ordersList) {
            var currentOrder = ordersTireRepository.findByName(order.replace(" ", "_"))
                    .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ",
                            order.replace("_", " ")));
            ordersTireService.add(currentOrder);
        }

        Integer price = bookingOrderRequest.getPrice();
        String wheelR = bookingOrderRequest.getWheelR();

        if (price == null || price == 0) {
            price = orderService.getTireOrderTimePrice(ordersList, wheelR).getSecond();
        }

        String comments = bookingOrderRequest.getComments();
        String autoNumber = bookingOrderRequest.getAutoNumber();
        String sale = bookingOrderRequest.getSale();
        String administrator = bookingOrderRequest.getAdministrator();
        String specialist = bookingOrderRequest.getSpecialist();
        int bonuses = bookingOrderRequest.getBonuses();
        String currentStatus = bookingOrderRequest.getCurrentStatus();
        String userPhone = lastUserVersion.getPhone();

        if (lastUserVersion.getFullName() != null && !lastUserVersion.getFullName().isBlank()) {
            userPhone += "; " + lastUserVersion.getFullName();
        }

        int autoType = bookingOrderRequest.getAutoType();

        var result = orderService.saveTireOrder(ordersTireService, startTime, endTime,
                administrator, specialist, boxNumber, bonuses, comments,
                autoNumber, autoType, userPhone, user, price, wheelR,
                OrderTypes.tireApp, currentStatus, 1, sale);


        Order newOrder = result.getFirst();
        OrderVersions orderCurrentVersions = result.getSecond();

        simpMessagingTemplate.convertAndSend(orderCurrentVersions);

        String operationName = "Book_tire_order";
        String descriptionMessage = "Пользователь с логином '" + userPhone + "' забронировал заказ на шиномонтаж";

        operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);

        log.info("bookTireOrder_v1. User with phone '{}' booked a car wash order.", userPhone);
        return ResponseEntity.ok(new OrderInfoResponse(newOrder.getId(), ordersList,
                startTime, endTime, administrator, specialist, boxNumber, autoNumber,
                autoType, bonuses, comments, newOrder.getUser().getId(), price, "tire", wheelR, currentStatus));
    }

    @PostMapping("/createWashingOrder_v1")
    @Transactional
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> creatingWashingOrder(@Valid @RequestBody CreatingWashingOrder bookingOrderRequest) {
        Date startTime = bookingOrderRequest.getStartTime();
        Date endTime = bookingOrderRequest.getEndTime();
        int boxNumber = bookingOrderRequest.getBoxNumber();
        if (!orderService.checkIfTimeFree(startTime, endTime, boxNumber)) {
            throw new TimeSlotUnavailableException(boxNumber);
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String administrator;
        Long userId = userDetails.getId();
        UserVersions user = userService.getActualUserVersionById(userId);

        administrator = (bookingOrderRequest.getAdministrator() != null && !bookingOrderRequest.getAdministrator().isEmpty())
                ? bookingOrderRequest.getAdministrator() : user.getPhone();

        List<OrdersWashing> ordersWashings = new ArrayList<>();

        List<String> ordersList = bookingOrderRequest.getOrders();
        for (var order : ordersList) {
            String dataBaseOrderName = order.replace(" ", "_");
            var currentOrder = ordersWashingRepository.findByName(dataBaseOrderName)
                    .orElseThrow(() -> new NotInDataBaseException("услуг мойки не найдена услуга: ", order.replace("_", " ")));
            ordersWashings.add(currentOrder);
        }

        Integer price = bookingOrderRequest.getPrice();
        int autoType = bookingOrderRequest.getAutoType();

        if (price == null || price == 0) {
            price = orderService.getWashingOrderPriceTime(ordersList, autoType).getSecond();
        }

        String comments = bookingOrderRequest.getComments();
        String autoNumber = bookingOrderRequest.getAutoNumber();
        String sale = bookingOrderRequest.getSale();
        String specialist = bookingOrderRequest.getSpecialist();
        int bonuses = bookingOrderRequest.getBonuses();
        String currentStatus = bookingOrderRequest.getCurrentStatus();
        String userContacts = bookingOrderRequest.getUserContacts();

        Pair<Order, OrderVersions> result = orderService.saveWashingOrder(ordersWashings, startTime,
                endTime, administrator, specialist, boxNumber,
                bonuses, comments, autoNumber, autoType,
                userContacts, null, price,
                OrderTypes.washSite, currentStatus, 1, sale);

        Order newOrder = result.getFirst();
        OrderVersions orderVersions = result.getSecond();
        String adminPhone = user.getPhone();

        String operationName = "Create_washing_order";
        String descriptionMessage = "Админ с телефоном '" + adminPhone + "' создал заказ на автомойку";

        operationsService.SaveUserOperation(operationName, user.getUser(), descriptionMessage, 1);
        log.info("createWashingOrder_v1. Admin with login '{}' created a car wash order.", adminPhone);

        return ResponseEntity.ok(new OrderInfoResponse(newOrder.getId(), ordersList,
                orderVersions.getStartTime(), orderVersions.getEndTime(), orderVersions.getAdministrator(),
                orderVersions.getSpecialist(), orderVersions.getBoxNumber(), orderVersions.getAutoNumber(),
                orderVersions.getAutoType(), orderVersions.getBonuses(), orderVersions.getComments(),
                price, "wash", "wash", orderVersions.getUserContacts(), orderVersions.getCurrentStatus()));
    }


    @PostMapping("/createPolishingOrder_v1")
    @Transactional
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> creatingPolishingOrder(@Valid @RequestBody CreatingPolishingOrder bookingOrderRequest) {
        Date startTime = bookingOrderRequest.getStartTime();
        Date endTime = bookingOrderRequest.getEndTime();
        int boxNumber = bookingOrderRequest.getBoxNumber();
        if (!orderService.checkIfTimeFree(startTime, endTime, boxNumber)) {
            throw new TimeSlotUnavailableException(boxNumber);
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String administrator;
        UserVersions user = userService.getActualUserVersionById(userDetails.getId());

        administrator = (bookingOrderRequest.getAdministrator() != null && !bookingOrderRequest.getAdministrator().isEmpty())
                ? bookingOrderRequest.getAdministrator() : user.getPhone();


        List<String> ordersList = bookingOrderRequest.getOrders();
        List<OrdersPolishing> ordersPolishings = new ArrayList<>();

        for (String order : ordersList) {
            OrdersPolishing currentOrder = ordersPolishingRepository.findByName(order.replace(" ", "_"))
                    .orElseThrow(() -> new NotInDataBaseException("услуг полировки не найдена услуга: ", order.replace("_", " ")));
            ordersPolishings.add(currentOrder);
        }

        Integer price = bookingOrderRequest.getPrice();
        int autoType = bookingOrderRequest.getAutoType();

        if (price == null || price == 0) {
            price = orderService.getPolishingOrderPriceAndTime(ordersList, autoType).getSecond();
        }

        String comments = bookingOrderRequest.getComments();
        String autoNumber = bookingOrderRequest.getAutoNumber();
        String sale = bookingOrderRequest.getSale();
        String specialist = bookingOrderRequest.getSpecialist();
        int bonuses = bookingOrderRequest.getBonuses();
        String currentStatus = bookingOrderRequest.getCurrentStatus();
        String userContacts = bookingOrderRequest.getUserContacts();

        Pair<Order, OrderVersions> result = orderService.savePolishingOrder(ordersPolishings, startTime,
                endTime, administrator, specialist, boxNumber, bonuses, comments,
                autoNumber, autoType, userContacts, null, price,
                OrderTypes.polishingSite, currentStatus, 1, sale);

        Order newOrder = result.getFirst();
        OrderVersions newOrderVersion = result.getSecond();
        String adminPhone = user.getPhone();

        String operationName = "Create_polishing_order";
        String descriptionMessage = "Админ с телефоном '" + adminPhone + "' создал заказ на полировку";

        operationsService.SaveUserOperation(operationName, user.getUser(), descriptionMessage, 1);

        log.info("createPolishingOrder_v1. Admin with login '{}' created a polishing order.", adminPhone);

        return ResponseEntity.ok(new OrderInfoResponse(newOrder.getId(), ordersList,
                newOrderVersion.getStartTime(), newOrderVersion.getEndTime(),
                newOrderVersion.getAdministrator(), newOrderVersion.getSpecialist(),
                newOrderVersion.getBoxNumber(), newOrderVersion.getAutoNumber(),
                newOrderVersion.getAutoType(), newOrderVersion.getBonuses(), newOrderVersion.getComments(),
                newOrderVersion.getPrice(), "polishing", "polishing",
                newOrderVersion.getUserContacts(), newOrderVersion.getCurrentStatus()));
    }

    @PostMapping("/createTireOrder_v1")
    @Transactional
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> createTireOrder(@Valid @RequestBody CreatingTireOrderRequest bookingOrderRequest) {
        Date startTime = bookingOrderRequest.getStartTime();
        Date endTime = bookingOrderRequest.getEndTime();
        int boxNumber = bookingOrderRequest.getBoxNumber();

        if (!orderService.checkIfTimeFree(startTime, endTime, boxNumber)) {
            throw new TimeSlotUnavailableException(boxNumber);
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String administrator;
        UserVersions user = userService.getActualUserVersionById(userDetails.getId());

        administrator = (bookingOrderRequest.getAdministrator() != null && !bookingOrderRequest.getAdministrator().isEmpty())
                ? bookingOrderRequest.getAdministrator() : user.getPhone();

        List<OrdersTire> ordersTireService = new ArrayList<>();
        List<String> ordersList = bookingOrderRequest.getOrders();

        for (var order : ordersList) {
            var currentOrder = ordersTireRepository.findByName(order.replace(" ", "_"))
                    .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", order.replace("_", " ")));
            ordersTireService.add(currentOrder);
        }

        Integer price = bookingOrderRequest.getPrice();
        String wheelR = bookingOrderRequest.getWheelR();

        if (price == null || price == 0) {
            price = orderService.getTireOrderTimePrice(ordersList, wheelR).getSecond();
        }

        String comments = bookingOrderRequest.getComments();
        String autoNumber = bookingOrderRequest.getAutoNumber();
        String sale = bookingOrderRequest.getSale();
        String specialist = bookingOrderRequest.getSpecialist();
        int bonuses = bookingOrderRequest.getBonuses();
        String currentStatus = bookingOrderRequest.getCurrentStatus();
        String userContacts = bookingOrderRequest.getUserContacts();
        int autoType = bookingOrderRequest.getAutoType();
        var result = orderService.saveTireOrder(ordersTireService, startTime, endTime,
                administrator, specialist, boxNumber, bonuses, comments, autoNumber,
                autoType, userContacts, null, price, wheelR,
                OrderTypes.tireSite, currentStatus, 1, sale);

        Order newOrder = result.getFirst();
        OrderVersions newOrderVersion = result.getSecond();
        String adminPhone = user.getPhone();
        String operationName = "Create_tire_order";
        String descriptionMessage = "Админ с телефоном '" + adminPhone + "' создал заказ на шиномонтаж";
        operationsService.SaveUserOperation(operationName, user.getUser(), descriptionMessage, 1);

        log.info("createTireOrder_v1. Admin with login '{}' created a tire order.", adminPhone);

        return ResponseEntity.ok(new OrderInfoResponse(newOrder.getId(), ordersList,
                newOrderVersion.getStartTime(), newOrderVersion.getEndTime(), newOrderVersion.getAdministrator(),
                newOrderVersion.getSpecialist(), newOrderVersion.getBoxNumber(), newOrderVersion.getAutoNumber(),
                newOrderVersion.getAutoType(), newOrderVersion.getBonuses(), newOrderVersion.getComments(),
                newOrderVersion.getPrice(), "tire", newOrderVersion.getWheelR(),
                newOrderVersion.getUserContacts(), newOrderVersion.getCurrentStatus()));
    }

}
