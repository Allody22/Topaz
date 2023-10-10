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
import ru.nsu.carwash_server.exceptions.NotInDataBaseException;
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
import ru.nsu.carwash_server.payload.response.MessageResponse;
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
import java.util.ArrayList;
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
        if (!orderService.checkIfTimeFree(bookingOrderRequest.getStartTime(), bookingOrderRequest.getEndTime(), bookingOrderRequest.getBoxNumber())) {
            log.warn("bookWashingOrder_v1. Booking attempt failed: This time slot from {} to {} in box {} is already taken.",
                    bookingOrderRequest.getStartTime(), bookingOrderRequest.getEndTime(), bookingOrderRequest.getBoxNumber());
            return ResponseEntity.badRequest().body(new MessageResponse("Это время в этом боксе уже занято"));
        }

        if (bookingOrderRequest.getBoxNumber() < 1 || bookingOrderRequest.getBoxNumber() > 3) {
            log.warn("bookWashingOrder_v1. Booking attempt failed: Box number {} cannot be used for washing.", bookingOrderRequest.getBoxNumber());
            return ResponseEntity.badRequest().body(new MessageResponse("Этот бокс не может быть использован для мойки"));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();

        User user = userService.getFullUserById(userId);


        UserVersions lastUserVersion = userService.getActualUserVersionById(userId);
        List<OrdersWashing> ordersWashings = new ArrayList<>();

        for (var order : bookingOrderRequest.getOrders()) {
            var currentOrder = ordersWashingRepository.findByName(order.replace(" ", "_"))
                    .orElseThrow(() -> new NotInDataBaseException("услуг мойки не найдена услуга: ", order));
            ordersWashings.add(currentOrder);
        }

        Integer price = bookingOrderRequest.getPrice();

        if (bookingOrderRequest.getPrice() == null || bookingOrderRequest.getPrice() == 0) {
            price = orderService.getWashingOrderPriceTime(bookingOrderRequest.getOrders(), bookingOrderRequest.getAutoType()).getPrice();
        }

        String orderType = "wash " + bookingOrderRequest.getOrderType();

        Pair<Order, OrderVersions> result = orderService.saveWashingOrder(
                ordersWashings, bookingOrderRequest.getStartTime(), bookingOrderRequest.getEndTime(),
                bookingOrderRequest.getAdministrator(), bookingOrderRequest.getSpecialist(),
                bookingOrderRequest.getBoxNumber(), bookingOrderRequest.getBonuses(),
                bookingOrderRequest.getComments(), bookingOrderRequest.getAutoNumber(),
                bookingOrderRequest.getAutoType(), lastUserVersion.getPhone(),
                user, price, orderType, bookingOrderRequest.getCurrentStatus(), 1, bookingOrderRequest.getSale());
        Order newOrder = result.getFirst();
        OrderVersions orderCurrentVersions = result.getSecond();

        simpMessagingTemplate.convertAndSend(orderCurrentVersions);

        String operationName = "Book_washing_order";
        String descriptionMessage = "Пользователь с логином '" + lastUserVersion.getPhone() + "' забронировал заказ на автомойку";

        operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);

        log.info("bookWashingOrder_v1. User with phone '{}' booked a car wash order.", lastUserVersion.getPhone());

        return ResponseEntity.ok(new OrderInfoResponse(newOrder.getId(), bookingOrderRequest.getOrders(),
                bookingOrderRequest.getStartTime(), bookingOrderRequest.getEndTime(),
                bookingOrderRequest.getAdministrator(), bookingOrderRequest.getSpecialist(),
                bookingOrderRequest.getBoxNumber(), bookingOrderRequest.getAutoNumber(),
                bookingOrderRequest.getAutoType(), bookingOrderRequest.getBonuses(), bookingOrderRequest.getComments(),
                newOrder.getUser().getId(), price, orderType, "wash", orderCurrentVersions.getCurrentStatus()));
    }

    @Transactional
    @PostMapping("/bookPolishingOrder_v1")
    public ResponseEntity<?> newPolishingOrder(@Valid @RequestBody BookingWashingPolishingOrderRequest bookingOrderRequest) {
        if (!orderService.checkIfTimeFree(bookingOrderRequest.getStartTime(), bookingOrderRequest.getEndTime(), bookingOrderRequest.getBoxNumber())) {
            log.warn("bookPolishingOrder_v1. Booking attempt failed: This time slot from {} to {} in box {} is already taken.",
                    bookingOrderRequest.getStartTime(), bookingOrderRequest.getEndTime(), bookingOrderRequest.getBoxNumber());
            return ResponseEntity.badRequest().body(new MessageResponse("Это время в этом боксе уже занято"));
        }

        if (bookingOrderRequest.getBoxNumber() != 5) {
            log.warn("bookPolishingOrder_v1. Booking attempt failed: Box number {} cannot be used for polishing.", bookingOrderRequest.getBoxNumber());
            return ResponseEntity.badRequest().body(new MessageResponse("Этот бокс не может быть использован для полировки"));
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();

        User user = userService.getFullUserById(userId);

        List<OrdersPolishing> ordersPolishings = new ArrayList<>();

        for (var order : bookingOrderRequest.getOrders()) {
            var currentOrder = ordersPolishingRepository.findByName(order.replace(" ", "_"))
                    .orElseThrow(() -> new NotInDataBaseException("услуг полировки не найдена услуга: ", order.replace("_", " ")));
            ordersPolishings.add(currentOrder);
        }

        Integer price = bookingOrderRequest.getPrice();

        if (bookingOrderRequest.getPrice() == null || bookingOrderRequest.getPrice() == 0) {
            price = orderService.getPolishingOrderPriceAndTime(bookingOrderRequest.getOrders(), bookingOrderRequest.getAutoType()).getPrice();
        }

        UserVersions lastUserVersion = userService.getActualUserVersionById(userId);

        Pair<Order, OrderVersions> result = orderService.savePolishingOrder(ordersPolishings, bookingOrderRequest.getStartTime(),
                bookingOrderRequest.getEndTime(), bookingOrderRequest.getAdministrator(),
                bookingOrderRequest.getSpecialist(), bookingOrderRequest.getBoxNumber(),
                bookingOrderRequest.getBonuses(), bookingOrderRequest.getComments(),
                bookingOrderRequest.getAutoNumber(),
                bookingOrderRequest.getAutoType(), lastUserVersion.getPhone(), user,
                price, OrderTypes.polishingApp, bookingOrderRequest.getCurrentStatus(), 1, bookingOrderRequest.getSale());

        Order newOrder = result.getFirst();
        OrderVersions orderCurrentVersions = result.getSecond();

        simpMessagingTemplate.convertAndSend(orderCurrentVersions);

        String operationName = "Book_polishing_order";
        String descriptionMessage = "Пользователь с логином '" + lastUserVersion.getPhone() + "' забронировал заказ на полировку";


        operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);

        log.info("bookPolishingOrder_v1. User with phone '{}' booked a car wash order.", lastUserVersion.getPhone());

        return ResponseEntity.ok(new OrderInfoResponse(newOrder.getId(), bookingOrderRequest.getOrders(),
                bookingOrderRequest.getStartTime(), bookingOrderRequest.getEndTime(), bookingOrderRequest.getAdministrator(),
                bookingOrderRequest.getSpecialist(), bookingOrderRequest.getBoxNumber(), bookingOrderRequest.getAutoNumber(),
                bookingOrderRequest.getAutoType(), bookingOrderRequest.getBonuses(), bookingOrderRequest.getComments(),
                newOrder.getUser().getId(), price, bookingOrderRequest.getOrderType(), "polishing", orderCurrentVersions.getCurrentStatus()));
    }

    @Transactional
    @PostMapping("/bookTireOrder_v1")
    public ResponseEntity<?> newTireOrder(@Valid @RequestBody BookingTireOrderRequest bookingOrderRequest) {
        if (!orderService.checkIfTimeFree(bookingOrderRequest.getStartTime(), bookingOrderRequest.getEndTime(), bookingOrderRequest.getBoxNumber())) {
            log.warn("bookTireOrder_v1. Booking attempt failed: This time slot from {} to {} in box {} is already taken.",
                    bookingOrderRequest.getStartTime(), bookingOrderRequest.getEndTime(), bookingOrderRequest.getBoxNumber());
            return ResponseEntity.badRequest().body(new MessageResponse("Это время в этом боксе уже занято"));
        }

        if (bookingOrderRequest.getBoxNumber() != 0) {
            log.warn("bookTireOrder_v1. Booking attempt failed: Box number {} cannot be used for tire.", bookingOrderRequest.getBoxNumber());
            return ResponseEntity.badRequest().body(new MessageResponse("Этот бокс не может быть использован для шиномонтажа"));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();

        User user = userService.getFullUserById(userId);

        List<OrdersTire> ordersTireService = new ArrayList<>();

        for (var order : bookingOrderRequest.getOrders()) {
            var currentOrder = ordersTireRepository.findByName(order.replace(" ", "_"))
                    .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ",
                            order.replace("_", " ")));
            ordersTireService.add(currentOrder);
        }

        Integer price = bookingOrderRequest.getPrice();

        if (bookingOrderRequest.getPrice() == null || bookingOrderRequest.getPrice() == 0) {
            price = orderService.getTireOrderTimePrice(bookingOrderRequest.getOrders(), bookingOrderRequest.getWheelR()).getPrice();
        }

        UserVersions lastUserVersion = userService.getActualUserVersionById(userId);

        var result = orderService.saveTireOrder(ordersTireService, bookingOrderRequest.getStartTime(),
                bookingOrderRequest.getEndTime(), bookingOrderRequest.getAdministrator(),
                bookingOrderRequest.getSpecialist(), bookingOrderRequest.getBoxNumber(),
                bookingOrderRequest.getBonuses(), bookingOrderRequest.getComments(),
                bookingOrderRequest.getAutoNumber(), bookingOrderRequest.getAutoType(),
                lastUserVersion.getPhone(), user, price, bookingOrderRequest.getWheelR(),
                OrderTypes.tireApp, bookingOrderRequest.getCurrentStatus(), 1, bookingOrderRequest.getSale());


        Order newOrder = result.getFirst();
        OrderVersions orderCurrentVersions = result.getSecond();

        simpMessagingTemplate.convertAndSend(orderCurrentVersions);

        String operationName = "Book_tire_order";
        String descriptionMessage = "Пользователь с логином '" + lastUserVersion.getPhone() + "' забронировал заказ на шиномонтаж";

        operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);

        log.info("bookTireOrder_v1. User with phone '{}' booked a car wash order.", lastUserVersion.getPhone());
        return ResponseEntity.ok(new OrderInfoResponse(newOrder.getId(), bookingOrderRequest.getOrders(),
                bookingOrderRequest.getStartTime(), bookingOrderRequest.getEndTime(), bookingOrderRequest.getAdministrator(),
                bookingOrderRequest.getSpecialist(), bookingOrderRequest.getBoxNumber(), bookingOrderRequest.getAutoNumber(),
                bookingOrderRequest.getAutoType(), bookingOrderRequest.getBonuses(), bookingOrderRequest.getComments(),
                newOrder.getUser().getId(), price, "tire", bookingOrderRequest.getWheelR(), orderCurrentVersions.getCurrentStatus()));
    }

    @PostMapping("/createWashingOrder_v1")
    @Transactional
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> creatingWashingOrder(@Valid @RequestBody CreatingWashingOrder bookingOrderRequest) {
        if (!orderService.checkIfTimeFree(bookingOrderRequest.getStartTime(), bookingOrderRequest.getEndTime(), bookingOrderRequest.getBoxNumber())) {
            log.warn("createWashingOrder_v1. Booking attempt failed: This time slot from {} to {} in box {} is already taken.",
                    bookingOrderRequest.getStartTime(), bookingOrderRequest.getEndTime(), bookingOrderRequest.getBoxNumber());
            return ResponseEntity.badRequest().body(new MessageResponse("Это время в этом боксе уже занято"));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String administrator;
        UserVersions user = userService.getActualUserVersionById(userDetails.getId());

        administrator = (bookingOrderRequest.getAdministrator() != null && !bookingOrderRequest.getAdministrator().isEmpty())
                ? bookingOrderRequest.getAdministrator() : user.getPhone();

        List<OrdersWashing> ordersWashings = new ArrayList<>();

        for (var order : bookingOrderRequest.getOrders()) {
            String dataBaseOrderName = order.replace(" ", "_");
            var currentOrder = ordersWashingRepository.findByName(dataBaseOrderName)
                    .orElseThrow(() -> new NotInDataBaseException("услуг мойки не найдена услуга: ", order.replace("_", " ")));
            ordersWashings.add(currentOrder);
        }

        Integer price = bookingOrderRequest.getPrice();

        if (bookingOrderRequest.getPrice() == null || bookingOrderRequest.getPrice() == 0) {
            price = orderService.getWashingOrderPriceTime(bookingOrderRequest.getOrders(), bookingOrderRequest.getAutoType()).getPrice();
        }

        Pair<Order, OrderVersions> result = orderService.saveWashingOrder(ordersWashings, bookingOrderRequest.getStartTime(),
                bookingOrderRequest.getEndTime(), administrator,
                bookingOrderRequest.getSpecialist(), bookingOrderRequest.getBoxNumber(),
                bookingOrderRequest.getBonuses(), bookingOrderRequest.getComments(),
                bookingOrderRequest.getAutoNumber(), bookingOrderRequest.getAutoType(),
                bookingOrderRequest.getUserContacts(), null, price,
                OrderTypes.washSite, bookingOrderRequest.getCurrentStatus(), 1, bookingOrderRequest.getSale());

        Order newOrder = result.getFirst();
        OrderVersions orderVersions = result.getSecond();

        String operationName = "Create_washing_order";
        String descriptionMessage = "Админ с телефоном '" + user.getPhone() + "' создал заказ на автомойку";

        operationsService.SaveUserOperation(operationName, user.getUser(), descriptionMessage, 1);
        log.info("createWashingOrder_v1. Admin with login '{}' created a car wash order.", user.getPhone());

        return ResponseEntity.ok(new OrderInfoResponse(newOrder.getId(), bookingOrderRequest.getOrders(),
                orderVersions.getStartTime(), orderVersions.getEndTime(), orderVersions.getAdministrator(), bookingOrderRequest.getSpecialist(),
                orderVersions.getBoxNumber(), orderVersions.getAutoNumber(),
                orderVersions.getAutoType(), orderVersions.getBonuses(),
                orderVersions.getComments(), price, "wash", "wash",
                bookingOrderRequest.getUserContacts(), orderVersions.getCurrentStatus()));
    }


    @PostMapping("/createPolishingOrder_v1")
    @Transactional
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> creatingPolishingOrder(@Valid @RequestBody CreatingPolishingOrder bookingOrderRequest) {
        if (!orderService.checkIfTimeFree(bookingOrderRequest.getStartTime(), bookingOrderRequest.getEndTime(), bookingOrderRequest.getBoxNumber())) {
            log.warn("createPolishingOrder_v1. Booking attempt failed: This time slot from {} to {} in box {} is already taken.",
                    bookingOrderRequest.getStartTime(), bookingOrderRequest.getEndTime(), bookingOrderRequest.getBoxNumber());
            return ResponseEntity.badRequest().body(new MessageResponse("Это время в этом боксе уже занято"));
        }

        if (bookingOrderRequest.getBoxNumber() != 5) {
            return ResponseEntity.badRequest().body(new MessageResponse("Этот бокс не может быть использован для полировки"));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String administrator;
        UserVersions user = userService.getActualUserVersionById(userDetails.getId());

        administrator = (bookingOrderRequest.getAdministrator() != null && !bookingOrderRequest.getAdministrator().isEmpty())
                ? bookingOrderRequest.getAdministrator() : user.getPhone();


        List<OrdersPolishing> ordersPolishings = new ArrayList<>();

        for (String order : bookingOrderRequest.getOrders()) {
            OrdersPolishing currentOrder = ordersPolishingRepository.findByName(order.replace(" ", "_"))
                    .orElseThrow(() -> new NotInDataBaseException("услуг полировки не найдена услуга: ", order.replace("_", " ")));
            ordersPolishings.add(currentOrder);
        }

        Integer price = bookingOrderRequest.getPrice();

        if (bookingOrderRequest.getPrice() == null || bookingOrderRequest.getPrice() == 0) {
            price = orderService.getPolishingOrderPriceAndTime(bookingOrderRequest.getOrders(), bookingOrderRequest.getAutoType()).getPrice();
        }

        Pair<Order, OrderVersions> result = orderService.savePolishingOrder(ordersPolishings, bookingOrderRequest.getStartTime(),
                bookingOrderRequest.getEndTime(), administrator,
                bookingOrderRequest.getSpecialist(), bookingOrderRequest.getBoxNumber(),
                bookingOrderRequest.getBonuses(), bookingOrderRequest.getComments(),
                bookingOrderRequest.getAutoNumber(), bookingOrderRequest.getAutoType(),
                bookingOrderRequest.getUserContacts(), null, price,
                OrderTypes.polishingSite, bookingOrderRequest.getCurrentStatus(), 1, bookingOrderRequest.getSale());

        Order newOrder = result.getFirst();
        OrderVersions newOrderVersion = result.getSecond();

        String operationName = "Create_polishing_order";
        String descriptionMessage = "Админ с телефоном '" + user.getPhone() + "' создал заказ на полировку";

        operationsService.SaveUserOperation(operationName, user.getUser(), descriptionMessage, 1);

        log.info("createPolishingOrder_v1. Admin with login '{}' created a polishing order.", user.getPhone());

        return ResponseEntity.ok(new OrderInfoResponse(newOrder.getId(), bookingOrderRequest.getOrders(),
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
        if (!orderService.checkIfTimeFree(bookingOrderRequest.getStartTime(), bookingOrderRequest.getEndTime(), bookingOrderRequest.getBoxNumber())) {
            log.warn("createTireOrder_v1. Booking attempt failed: This time slot from {} to {} in box {} is already taken.",
                    bookingOrderRequest.getStartTime(), bookingOrderRequest.getEndTime(), bookingOrderRequest.getBoxNumber());
            return ResponseEntity.badRequest().body(new MessageResponse("Это время в этом боксе уже занято"));
        }
        if (bookingOrderRequest.getBoxNumber() != 0) {
            return ResponseEntity.badRequest().body(new MessageResponse("Этот бокс не может быть использован для шиномонтажа"));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String administrator;
        UserVersions user = userService.getActualUserVersionById(userDetails.getId());

        administrator = (bookingOrderRequest.getAdministrator() != null && !bookingOrderRequest.getAdministrator().isEmpty())
                ? bookingOrderRequest.getAdministrator() : user.getPhone();

        List<OrdersTire> ordersTireService = new ArrayList<>();

        for (var order : bookingOrderRequest.getOrders()) {
            var currentOrder = ordersTireRepository.findByName(order.replace(" ", "_"))
                    .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", order.replace("_", " ")));
            ordersTireService.add(currentOrder);
        }

        Integer price = bookingOrderRequest.getPrice();

        if (bookingOrderRequest.getPrice() == null || bookingOrderRequest.getPrice() == 0) {
            price = orderService.getTireOrderTimePrice(bookingOrderRequest.getOrders(), bookingOrderRequest.getWheelR()).getPrice();
        }

        var result = orderService.saveTireOrder(ordersTireService,
                bookingOrderRequest.getStartTime(), bookingOrderRequest.getEndTime(),
                administrator, bookingOrderRequest.getSpecialist(),
                bookingOrderRequest.getBoxNumber(), bookingOrderRequest.getBonuses(),
                bookingOrderRequest.getComments(), bookingOrderRequest.getAutoNumber(),
                bookingOrderRequest.getAutoType(), bookingOrderRequest.getUserContacts(),
                null, price, bookingOrderRequest.getWheelR(),
                OrderTypes.tireSite, bookingOrderRequest.getCurrentStatus(), 1, bookingOrderRequest.getSale());

        Order newOrder = result.getFirst();
        OrderVersions newOrderVersion = result.getSecond();

        String operationName = "Create_tire_order";
        String descriptionMessage = "Админ с телефоном '" + user.getPhone() + "' создал заказ на шиномонтаж";
        operationsService.SaveUserOperation(operationName, user.getUser(), descriptionMessage, 1);

        log.info("createTireOrder_v1. Admin with login '{}' created a tire order.", user.getPhone());

        return ResponseEntity.ok(new OrderInfoResponse(newOrder.getId(), bookingOrderRequest.getOrders(),
                newOrderVersion.getStartTime(), newOrderVersion.getEndTime(), newOrderVersion.getAdministrator(),
                newOrderVersion.getSpecialist(), newOrderVersion.getBoxNumber(), newOrderVersion.getAutoNumber(),
                newOrderVersion.getAutoType(), newOrderVersion.getBonuses(), newOrderVersion.getComments(),
                newOrderVersion.getPrice(), "tire", newOrderVersion.getWheelR(),
                newOrderVersion.getUserContacts(), newOrderVersion.getCurrentStatus()));
    }

}
