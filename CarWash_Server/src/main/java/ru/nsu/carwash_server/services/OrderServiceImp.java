package ru.nsu.carwash_server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import ru.nsu.carwash_server.exceptions.InvalidOrderTypeException;
import ru.nsu.carwash_server.exceptions.NotInDataBaseException;
import ru.nsu.carwash_server.models.orders.Order;
import ru.nsu.carwash_server.models.orders.OrderVersions;
import ru.nsu.carwash_server.models.orders.OrdersPolishing;
import ru.nsu.carwash_server.models.orders.OrdersTire;
import ru.nsu.carwash_server.models.orders.OrdersWashing;
import ru.nsu.carwash_server.models.secondary.constants.OrderStatuses;
import ru.nsu.carwash_server.models.secondary.helpers.AllOrderTypes;
import ru.nsu.carwash_server.models.users.User;
import ru.nsu.carwash_server.payload.request.NewServiceRequest;
import ru.nsu.carwash_server.payload.request.UpdateOrderInfoRequest;
import ru.nsu.carwash_server.payload.request.UpdatePolishingServiceRequest;
import ru.nsu.carwash_server.payload.request.UpdateTireServiceRequest;
import ru.nsu.carwash_server.payload.request.UpdateWashingServiceRequest;
import ru.nsu.carwash_server.payload.response.ConnectedOrdersResponse;
import ru.nsu.carwash_server.repository.orders.OrderVersionsRepository;
import ru.nsu.carwash_server.repository.orders.OrdersPolishingRepository;
import ru.nsu.carwash_server.repository.orders.OrdersRepository;
import ru.nsu.carwash_server.repository.orders.OrdersTireRepository;
import ru.nsu.carwash_server.repository.orders.OrdersWashingRepository;
import ru.nsu.carwash_server.services.interfaces.OrderService;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

@Service
public class OrderServiceImp implements OrderService {
    private final OrdersRepository ordersRepository;
    private final OrdersPolishingRepository ordersPolishingRepository;
    private final OrdersTireRepository ordersTireRepository;
    private final OrdersWashingRepository ordersWashingRepository;

    private final OrderVersionsRepository orderVersionsRepository;

    @Autowired
    public OrderServiceImp(OrdersRepository ordersRepository,
                           OrdersWashingRepository ordersWashingRepository,
                           OrdersTireRepository ordersTireRepository,
                           OrdersPolishingRepository ordersPolishingRepository,
                           OrderVersionsRepository orderVersionsRepository) {
        this.ordersRepository = ordersRepository;
        this.ordersWashingRepository = ordersWashingRepository;
        this.orderVersionsRepository = orderVersionsRepository;
        this.ordersPolishingRepository = ordersPolishingRepository;
        this.ordersTireRepository = ordersTireRepository;
    }

    public List<String> getAllWashingOrdersByRole(String role) {
        return ordersWashingRepository.findAllByRole(role)
                .orElse(null);
    }

    @Transactional
    @CacheEvict(value = "washingOrders", allEntries = true)
    public String updateWashingService(UpdateWashingServiceRequest updateWashingServiceRequest) {
        String serviceName = updateWashingServiceRequest.getName();
        ordersWashingRepository.findByName(updateWashingServiceRequest.getName())
                .orElseThrow(() -> new NotInDataBaseException("услуг мойки не найдена услуга ", serviceName.replace("_", " ")));
        int priceFirstType = updateWashingServiceRequest.getPriceFirstType();
        int priceSecondType = updateWashingServiceRequest.getPriceSecondType();
        int priceThirdType = updateWashingServiceRequest.getPriceThirdType();
        int timeFirstType = updateWashingServiceRequest.getTimeFirstType();
        int timeSecondType = updateWashingServiceRequest.getTimeSecondType();
        int timeThirdType = updateWashingServiceRequest.getTimeThirdType();
        String serviceRole = updateWashingServiceRequest.getRole();

        ordersWashingRepository.updateWashingServiceInfo(serviceName, priceFirstType, priceSecondType, priceThirdType,
                timeFirstType, timeSecondType, timeThirdType, serviceRole);

        String descriptionMessage = getPolishingWashingOrderChangingInfo(priceFirstType,
                priceSecondType, priceThirdType, timeFirstType, timeSecondType, timeThirdType, "Обновлена услуга", serviceName);

        return descriptionMessage;
    }

    @Transactional
    @CacheEvict(value = "polishingOrders", allEntries = true)
    public String updatePolishingService(UpdatePolishingServiceRequest updatePolishingServiceRequest) {
        String serviceName = updatePolishingServiceRequest.getName();
        ordersPolishingRepository.findByName(serviceName)
                .orElseThrow(() -> new NotInDataBaseException("услуг полировки не найдена услуга ", serviceName.replace("_", " ")));

        int priceFirstType = updatePolishingServiceRequest.getPriceFirstType();
        int priceSecondType = updatePolishingServiceRequest.getPriceSecondType();
        int priceThirdType = updatePolishingServiceRequest.getPriceThirdType();
        int timeFirstType = updatePolishingServiceRequest.getTimeFirstType();
        int timeSecondType = updatePolishingServiceRequest.getTimeSecondType();
        int timeThirdType = updatePolishingServiceRequest.getTimeThirdType();

        ordersPolishingRepository.updatePolishingOrder(serviceName, priceFirstType, priceSecondType, priceThirdType,
                timeFirstType, timeSecondType, timeThirdType);


        return getPolishingWashingOrderChangingInfo(timeFirstType,
                priceSecondType, priceThirdType, timeFirstType, timeSecondType, timeThirdType, "Обновлена услуга", serviceName);
    }


    @Transactional
    @CacheEvict(value = "tireOrders", allEntries = true)
    public String updateTireService(UpdateTireServiceRequest updateTireServiceRequest) {
        String serviceName = updateTireServiceRequest.getName();

        ordersTireRepository.findByName(serviceName)
                .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга ", serviceName.replace("_", " ")));

        ordersTireRepository.updateTireOrderInfo(updateTireServiceRequest.getName(), updateTireServiceRequest.getPrice_r_13(),
                updateTireServiceRequest.getPrice_r_14(), updateTireServiceRequest.getPrice_r_15(),
                updateTireServiceRequest.getPrice_r_16(), updateTireServiceRequest.getPrice_r_17(),
                updateTireServiceRequest.getPrice_r_18(), updateTireServiceRequest.getPrice_r_19(), updateTireServiceRequest.getPrice_r_20(),
                updateTireServiceRequest.getPrice_r_21(), updateTireServiceRequest.getPrice_r_22(),
                updateTireServiceRequest.getTime_r_13(), updateTireServiceRequest.getTime_r_14(),
                updateTireServiceRequest.getTime_r_15(), updateTireServiceRequest.getTime_r_16(),
                updateTireServiceRequest.getTime_r_17(), updateTireServiceRequest.getTime_r_18(),
                updateTireServiceRequest.getTime_r_19(), updateTireServiceRequest.getTime_r_20(),
                updateTireServiceRequest.getTime_r_21(), updateTireServiceRequest.getTime_r_22(),
                updateTireServiceRequest.getRole());

        return "Услуга '" + updateTireServiceRequest.getName().replace("_", " ")
                + "' изменена";
    }

    @Cacheable(value = "washingOrders")
    public List<OrdersWashing> findAllWashingService() {
        return ordersWashingRepository.findAll();
    }

    @Cacheable(value = "polishingOrders")
    public List<OrdersPolishing> findAllPolishingService() {
        return ordersPolishingRepository.findAll();
    }

    @Cacheable(value = "tireOrders")
    public List<OrdersTire> findAllTireService() {
        return ordersTireRepository.findAll();
    }

    public Pair<Integer, Integer> getTireOrderTimePrice(List<String> orderArray, String wheelR) {
        int price = 0;
        int time = 15;
        switch (wheelR) {
            case "R13" -> {
                for (var item : orderArray) {
                    var currentOrder = ordersTireRepository.findByName(item.replace(" ", "_"))
                            .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", item.replace("_", " ")));
                    price += currentOrder.getPrice_r_13();
                    time += currentOrder.getTime_r_13();
                }
            }
            case "R14" -> {
                for (var item : orderArray) {
                    var currentOrder = ordersTireRepository.findByName(item.replace(" ", "_"))
                            .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", item.replace("_", " ")));
                    price += currentOrder.getPrice_r_14();
                    time += currentOrder.getTime_r_14();
                }
            }
            case "R15" -> {
                for (var item : orderArray) {
                    var currentOrder = ordersTireRepository.findByName(item.replace(" ", "_"))
                            .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", item.replace("_", " ")));
                    price += currentOrder.getPrice_r_15();
                    time += currentOrder.getTime_r_15();
                }
            }
            case "R16" -> {
                for (var item : orderArray) {
                    var currentOrder = ordersTireRepository.findByName(item.replace(" ", "_"))
                            .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", item.replace("_", " ")));
                    price += currentOrder.getPrice_r_16();
                    time += currentOrder.getTime_r_16();
                }
            }
            case "R17" -> {
                for (var item : orderArray) {
                    var currentOrder = ordersTireRepository.findByName(item.replace(" ", "_"))
                            .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", item.replace("_", " ")));
                    price += currentOrder.getPrice_r_17();
                    time += currentOrder.getTime_r_17();
                }
            }
            case "R18" -> {
                for (var item : orderArray) {
                    var currentOrder = ordersTireRepository.findByName(item.replace(" ", "_"))
                            .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", item.replace("_", " ")));
                    price += currentOrder.getPrice_r_18();
                    time += currentOrder.getTime_r_18();
                }
            }
            case "R19" -> {
                for (var item : orderArray) {
                    var currentOrder = ordersTireRepository.findByName(item.replace(" ", "_"))
                            .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", item.replace("_", " ")));
                    price += currentOrder.getPrice_r_19();
                    time += currentOrder.getTime_r_19();
                }
            }
            case "R20" -> {
                for (var item : orderArray) {
                    var currentOrder = ordersTireRepository.findByName(item.replace(" ", "_"))
                            .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", item.replace("_", " ")));
                    price += currentOrder.getPrice_r_20();
                    time += currentOrder.getTime_r_20();
                }
            }
            case "R21" -> {
                for (var item : orderArray) {
                    var currentOrder = ordersTireRepository.findByName(item.replace(" ", "_"))
                            .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", item.replace("_", " ")));
                    price += currentOrder.getPrice_r_21();
                    time += currentOrder.getTime_r_21();
                }
            }
            case "R22" -> {
                for (var item : orderArray) {
                    var currentOrder = ordersTireRepository.findByName(item.replace(" ", "_"))
                            .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", item.replace("_", " ")));
                    price += currentOrder.getPrice_r_22();
                    time += currentOrder.getTime_r_22();
                }
            }
        }
        return Pair.of(time, price);
    }

    public Pair<Integer, Integer> getWashingOrderPriceTime(List<String> orderArray, int bodyType) {
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
        return Pair.of(time, price);
    }

    public Pair<Integer, Integer> getPolishingOrderPriceAndTime(List<String> orderArray, int bodyType) {
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
        return Pair.of(time, price);
    }

    public ConnectedOrdersResponse actualWashingOrders(String orderName) {
        List<String> mainOrders = ordersWashingRepository.findAllMain(orderName + "Solo")
                .orElse(null);
        List<String> connectedOrders = ordersWashingRepository.findAllAssociated(orderName)
                .orElse(null);
        return new ConnectedOrdersResponse(mainOrders, connectedOrders);
    }

    @CacheEvict(value = "tireOrders", allEntries = true)
    public Pair<String, OrdersTire> createTireService(NewServiceRequest newServiceRequest) {
        OrdersTire ordersTire = OrdersTire.builder()
                .name(newServiceRequest.getName())
                .price_r_13(newServiceRequest.getPrice_r_13())
                .price_r_14(newServiceRequest.getPrice_r_14())
                .price_r_15(newServiceRequest.getPrice_r_15())
                .price_r_16(newServiceRequest.getPrice_r_16())
                .price_r_17(newServiceRequest.getPrice_r_17())
                .price_r_18(newServiceRequest.getPrice_r_18())
                .price_r_19(newServiceRequest.getPrice_r_19())
                .price_r_20(newServiceRequest.getPrice_r_20())
                .price_r_21(newServiceRequest.getPrice_r_21())
                .price_r_22(newServiceRequest.getPrice_r_22())
                .time_r_13(newServiceRequest.getTime_r_13())
                .time_r_14(newServiceRequest.getTime_r_14())
                .time_r_15(newServiceRequest.getTime_r_15())
                .time_r_16(newServiceRequest.getTime_r_16())
                .time_r_17(newServiceRequest.getTime_r_17())
                .time_r_18(newServiceRequest.getTime_r_18())
                .time_r_19(newServiceRequest.getTime_r_19())
                .time_r_20(newServiceRequest.getTime_r_20())
                .time_r_21(newServiceRequest.getTime_r_21())
                .time_r_22(newServiceRequest.getTime_r_22())
                .role(newServiceRequest.getRole())
                .build();

        String descriptionMessage = "Создана услуга шиномонтажа'" + newServiceRequest.getName().replace("_", " ");

        OrdersTire savedService = ordersTireRepository.save(ordersTire);
        return Pair.of(descriptionMessage, savedService);
    }

    @CacheEvict(value = "polishingOrders", allEntries = true)
    public Pair<String, OrdersPolishing> createPolishingService(NewServiceRequest newServiceRequest) {
        OrdersPolishing ordersPolishing = OrdersPolishing.builder().
                name(newServiceRequest.getName())
                .priceFirstType(newServiceRequest.getPriceFirstType())
                .priceSecondType(newServiceRequest.getPriceSecondType())
                .priceThirdType(newServiceRequest.getPriceThirdType())
                .timeFirstType(newServiceRequest.getTimeFirstType())
                .timeSecondType(newServiceRequest.getTimeSecondType())
                .timeThirdType(newServiceRequest.getTimeThirdType())
                .build();

        String descriptionMessage = getPolishingWashingOrderChangingInfo(ordersPolishing.getPriceFirstType(), ordersPolishing.getPriceSecondType(),
                ordersPolishing.getPriceThirdType(), ordersPolishing.getTimeFirstType(), ordersPolishing.getTimeSecondType(),
                ordersPolishing.getTimeThirdType(), "Создана услуга полировки '", ordersPolishing.getName().replace("_", " "));

        OrdersPolishing savedService = ordersPolishingRepository.save(ordersPolishing);
        return Pair.of(descriptionMessage, savedService);
    }

    @CacheEvict(value = "washingOrders", allEntries = true)
    public Pair<String, OrdersWashing> createWashingService(NewServiceRequest newServiceRequest) {
        StringJoiner joiner = new StringJoiner(";");
        for (String element : newServiceRequest.getIncludedIn()) {
            joiner.add(element);
        }

        String serviceRoleInApp = joiner.toString();
        OrdersWashing ordersWashing = OrdersWashing.builder().
                name(newServiceRequest.getName())
                .priceFirstType(newServiceRequest.getPriceFirstType())
                .priceSecondType(newServiceRequest.getPriceSecondType())
                .priceThirdType(newServiceRequest.getPriceThirdType())
                .timeFirstType(newServiceRequest.getTimeFirstType())
                .timeSecondType(newServiceRequest.getTimeSecondType())
                .timeThirdType(newServiceRequest.getTimeThirdType())
                .associatedOrder(serviceRoleInApp)
                .role(newServiceRequest.getRole())
                .build();

        String descriptionMessage = getPolishingWashingOrderChangingInfo(ordersWashing.getPriceFirstType(), ordersWashing.getPriceSecondType(),
                ordersWashing.getPriceThirdType(), ordersWashing.getTimeFirstType(), ordersWashing.getTimeSecondType(),
                ordersWashing.getTimeThirdType(), "Создана услуга мойки '", ordersWashing.getName().replace("_", " "));

        OrdersWashing savedService = ordersWashingRepository.save(ordersWashing);
        return Pair.of(descriptionMessage, savedService);
    }


    public String getPolishingWashingOrderChangingInfo(Integer priceFirstType, Integer priceSecondType, Integer priceThirdType,
                                                       Integer timeFirstType, Integer timeSecondType, Integer tineThirdType,
                                                       String context, String orderName) {
        String newPriceFirstType = (priceFirstType != null) ?
                " цену за 1 тип: '" + priceFirstType + "', " : null;

        String newPriceSecondType = (priceSecondType != null) ?
                " цену за 2 тип: '" + priceSecondType + "', " : null;

        String newPriceThirdType = (priceThirdType != null) ?
                " цену за 3 тип: '" + priceThirdType + "', " : null;

        String newTimeFirstType = (timeFirstType != null) ?
                " время за 1 тип: '" + timeSecondType + "', " : null;

        String newTimeSecondType = (timeSecondType != null) ?
                " время за 2 тип: '" + timeSecondType + "', " : null;

        String newTimeThirdType = (tineThirdType != null) ?
                " время за 3 тип: '" + tineThirdType + "', " : null;

        return context + " '" + orderName.replace("_", " ")
                + "', получившая " + newPriceFirstType + newPriceSecondType + newPriceThirdType +
                newTimeFirstType + newTimeSecondType + newTimeThirdType;
    }

    @Transactional
    public void updateOrderInfo(UpdateOrderInfoRequest updateOrderInfoRequest) {
        Long orderId = updateOrderInfoRequest.getOrderId();

        String orderType = updateOrderInfoRequest.getOrderType();
        Order order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new NotInDataBaseException("заказов не найден заказ с айди: ", orderId.toString()));

        var actualOrderVersion = getActualOrderVersion(orderId);

        List<String> ordersList = updateOrderInfoRequest.getOrders();

        List<OrdersWashing> ordersWashings = new ArrayList<>();
        List<OrdersPolishing> ordersPolishings = new ArrayList<>();
        List<OrdersTire> ordersTires = new ArrayList<>();
        if (orderType.contains("tire")) {
            for (var tireOrder : ordersList) {
                var service = ordersTireRepository.findByName(tireOrder.replace(" ", "_"))
                        .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", tireOrder.replace("_", " ")));
                ordersTires.add(service);
            }
        } else if (orderType.contains("polish")) {
            for (var polishOrder : ordersList) {
                var service = ordersPolishingRepository.findByName(polishOrder.replace(" ", "_"))
                        .orElseThrow(() -> new NotInDataBaseException("услуг полировки не найдена услуга: ", polishOrder.replace("_", " ")));
                ordersPolishings.add(service);
            }
        } else if (orderType.contains("wash")) {
            for (var washingOrders : ordersList) {
                var service = ordersWashingRepository.findByName(washingOrders.replace(" ", "_"))
                        .orElseThrow(() -> new NotInDataBaseException("услуг мойки не найдена услуга: ", washingOrders.replace("_", " ")));
                ordersWashings.add(service);
            }
        } else {
            throw new InvalidOrderTypeException(orderType);
        }

        AllOrderTypes allOrdersByTypes = copyOrdersByName(actualOrderVersion.getOrdersPolishings(),
                actualOrderVersion.getOrdersWashing(), actualOrderVersion.getOrdersTires());

        OrderVersions newOrderVersion = new OrderVersions(actualOrderVersion, updateOrderInfoRequest,
                ordersTires, ordersPolishings, ordersWashings, allOrdersByTypes.getOrdersTire(),
                allOrdersByTypes.getOrdersPolishing(), allOrdersByTypes.getOrdersWashing());

        order.addOrderVersion(newOrderVersion);
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new NotInDataBaseException("заказов не найден заказ с айди: ", orderId.toString()));
        var actualOrderVersion = getActualOrderVersion(orderId);
        String orderType = actualOrderVersion.getOrderType();

        List<OrdersWashing> ordersWashings = new ArrayList<>();
        List<OrdersPolishing> ordersPolishings = new ArrayList<>();
        List<OrdersTire> ordersTires = new ArrayList<>();
        if (orderType.contains("tire")) {
            for (var tireOrder : actualOrderVersion.getOrdersTires()) {
                var service = ordersTireRepository.findByName(tireOrder.getName())
                        .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", tireOrder.getName().replace("_", " ")));
                ordersTires.add(service);
            }
        } else if (orderType.contains("polish")) {
            for (var polishOrder : actualOrderVersion.getOrdersPolishings()) {
                var service = ordersPolishingRepository.findByName(polishOrder.getName().replace(" ", "_"))
                        .orElseThrow(() -> new NotInDataBaseException("услуг полировки не найдена услуга: ", polishOrder.getName().replace("_", " ")));
                ordersPolishings.add(service);
            }
        } else if (orderType.contains("wash")) {
            for (var washingOrders : actualOrderVersion.getOrdersWashing()) {
                var service = ordersWashingRepository.findByName(washingOrders.getName().replace(" ", "_"))
                        .orElseThrow(() -> new NotInDataBaseException("услуг мойки не найдена услуга: ", washingOrders.getName().replace("_", " ")));
                ordersWashings.add(service);
            }
        } else {
            throw new InvalidOrderTypeException(orderType);
        }

        AllOrderTypes allOrdersByTypes = copyOrdersByName(actualOrderVersion.getOrdersPolishings(),
                actualOrderVersion.getOrdersWashing(), actualOrderVersion.getOrdersTires());

        UpdateOrderInfoRequest updateOrderInfoRequest = new UpdateOrderInfoRequest();
        updateOrderInfoRequest.setCurrentStatus(OrderStatuses.cancelled);
        OrderVersions newOrderVersion = new OrderVersions(actualOrderVersion, updateOrderInfoRequest,
                ordersTires, ordersPolishings, ordersWashings, allOrdersByTypes.getOrdersTire(),
                allOrdersByTypes.getOrdersPolishing(), allOrdersByTypes.getOrdersWashing());
        order.addOrderVersion(newOrderVersion);

    }

    @Transactional
    public Pair<Order, OrderVersions> savePolishingOrder(List<OrdersPolishing> ordersPolishings, Date startTime,
                                                         Date endTime, String administrator, String specialist,
                                                         int boxNumber, int bonuses, String comments,
                                                         String autoNumber, int autoType, String userContacts,
                                                         User user, int price, String orderSource,
                                                         String currentStatus, int version, String sale) {
        Order newOrder;
        Date currentTime = new Date();
        OrderVersions orderVersions;
        List<OrderVersions> orderVersionsList = new ArrayList<>();
        if (user == null) {
            orderVersions = new OrderVersions(ordersPolishings, startTime,
                    endTime, currentTime, administrator, specialist,
                    boxNumber, bonuses, comments, autoNumber,
                    autoType, userContacts, orderSource, price, currentStatus, version, sale);
            orderVersionsList.add(orderVersions);
            newOrder = new Order();
            newOrder.setDateOfCreation(new Date());
            newOrder.setUser(null);

        } else {
            orderVersions = new OrderVersions(ordersPolishings, startTime,
                    endTime, currentTime, administrator, specialist,
                    boxNumber, bonuses, comments, autoNumber,
                    autoType, userContacts, orderSource, price,
                    currentStatus, version, sale);
            orderVersionsList.add(orderVersions);
            newOrder = new Order();
            newOrder.setDateOfCreation(new Date());
            newOrder.setUser(user);
        }
        newOrder.setOrderVersions(orderVersionsList);
        orderVersions.setOrder(newOrder);
        ordersRepository.save(newOrder);
        return Pair.of(newOrder, orderVersions);
    }

    @Transactional
    public Pair<Order, OrderVersions> saveTireOrder(List<OrdersTire> ordersTire, Date startTime, Date endTime,
                                                    String administrator, String specialist, int boxNumber,
                                                    int bonuses, String comments, String autoNumber,
                                                    int autoType, String userContacts, User user,
                                                    int price, String wheelR, String orderSource,
                                                    String currentStatus, int version, String sale) {
        Order newOrder;
        Date currentTime = new Date();
        OrderVersions orderVersions;
        List<OrderVersions> orderVersionsList = new ArrayList<>();
        if (user == null) {
            orderVersions = new OrderVersions(ordersTire, startTime,
                    endTime, currentTime, administrator, specialist,
                    boxNumber, bonuses, comments,
                    autoNumber, autoType, userContacts,
                    orderSource, price, wheelR, currentStatus, version, sale);
            orderVersionsList.add(orderVersions);
            newOrder = new Order();
            newOrder.setDateOfCreation(new Date());
            newOrder.setUser(null);
        } else {
            orderVersions = new OrderVersions(ordersTire, startTime,
                    endTime, currentTime, administrator, specialist,
                    boxNumber, bonuses, comments, autoNumber,
                    autoType, userContacts, orderSource, price,
                    wheelR, currentStatus, version, sale);
            orderVersionsList.add(orderVersions);
            newOrder = new Order();
            newOrder.setDateOfCreation(new Date());
            newOrder.setUser(user);
        }
        newOrder.setOrderVersions(orderVersionsList);
        orderVersions.setOrder(newOrder);

        ordersRepository.save(newOrder);
        return Pair.of(newOrder, orderVersions);
    }

    @Transactional
    public Pair<Order, OrderVersions> saveWashingOrder(List<OrdersWashing> ordersWashings, Date startTime,
                                                       Date endTime, String administrator, String specialist,
                                                       int boxNumber, int bonuses, String comments,
                                                       String autoNumber, int autoType, String userContacts,
                                                       User user, int price, String orderType,
                                                       String currentStatus, int version, String sale) {
        Order newOrder;
        Date currentTime = new Date();
        OrderVersions orderVersions;
        List<OrderVersions> orderVersionsList = new ArrayList<>();
        if (user == null) {
            orderVersions = new OrderVersions(ordersWashings, startTime,
                    endTime, currentTime, administrator, specialist,
                    boxNumber, bonuses, comments,
                    autoNumber, autoType, userContacts,
                    orderType, currentStatus, price, version, sale);
            orderVersionsList.add(orderVersions);
            newOrder = new Order();
            newOrder.setDateOfCreation(new Date());
            newOrder.setUser(null);
        } else {
            orderVersions = new OrderVersions(ordersWashings, startTime,
                    endTime, currentTime, administrator, specialist,
                    boxNumber, bonuses, comments,
                    autoNumber, autoType, userContacts,
                    orderType, currentStatus, price, version, sale);
            orderVersionsList.add(orderVersions);
            newOrder = new Order();
            newOrder.setDateOfCreation(new Date());
            newOrder.setUser(user);
        }
        newOrder.setOrderVersions(orderVersionsList);
        orderVersions.setOrder(newOrder);

        ordersRepository.save(newOrder);
        return Pair.of(newOrder, orderVersions);
    }

    //Если true - то учитываем отменённые
    public List<OrderVersions> getOrdersInTimeInterval(Date startTime, Date endTime, Integer box, boolean flag) {
        if (box == null) {
            if (flag) {
                return orderVersionsRepository.getLatestOrderVersionsInOneDayFullWithCancelled(startTime, endTime);
            }
            return orderVersionsRepository.getLatestOrderVersionsInOneDayFull(startTime, endTime);
        } else {
            if (flag) {
                return orderVersionsRepository.getLatestOrderVersionsInOneDayFullInBoxWithCancelled(startTime, endTime, box);
            }
            return orderVersionsRepository.getLatestOrderVersionsInOneDayFullInBox(startTime, endTime, box);

        }
    }

    public List<OrderVersions> getOrdersCreatedAt(Date firstDate, Date secondDate, boolean flag) {
        if (flag) {
            return orderVersionsRepository.getLatestVersionByDateOfCreationWithCancelled(firstDate, secondDate);
        } else {
            return orderVersionsRepository.getLatestVersionByDateOfCreation(firstDate, secondDate);
        }
    }

    public List<Order> getUserOrdersInTimeInterval(Date firstDate, Date secondDate, Long userId) {
        return ordersRepository.findAllByDateOfCreationAndUser(firstDate, secondDate, userId);
    }


    public List<OrderVersions> getNotMadeOrders(boolean flag) {
        if (flag) {
            return orderVersionsRepository.getLatestOrderVersionsWithStatusNotDoneWithCancelled();
        } else {
            return orderVersionsRepository.getLatestOrderVersionsWithStatusNotDone();
        }
    }

    public boolean checkIfTimeFree(Date startTime, Date endTime, Integer box) {
        return orderVersionsRepository.getOrderLatestVersionAroundThisTimeInterval
                (startTime, endTime, box).isEmpty();
    }

    public Order findById(Long id) {
        if (id == null) {
            return null;
        }
        Optional<Order> currentOrder = ordersRepository.findById(id);
        return currentOrder.orElse(null);
    }

    public OrderVersions getActualOrderVersion(@NotNull Long id) {

        return orderVersionsRepository.findLatestVersionByOrderId(id)
                .orElseThrow(() -> new NotInDataBaseException("заказов не найден заказ с айди: ", id.toString()));
    }

    public AllOrderTypes copyOrdersByName(List<OrdersPolishing> currentOrdersPolishing,
                                          List<OrdersWashing> currentOrdersWashing, List<OrdersTire> currentOrdersTire) {
        List<OrdersPolishing> ordersPolishing = new ArrayList<>();
        List<OrdersWashing> ordersWashing = new ArrayList<>();
        List<OrdersTire> ordersTire = new ArrayList<>();

        for (var order : currentOrdersTire) {
            var service = ordersTireRepository.findByName(order.getName().replace(" ", "_"))
                    .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", order.getName().replace("_", " ")));
            ordersTire.add(service);
        }
        for (var order : currentOrdersPolishing) {
            var service = ordersPolishingRepository.findByName(order.getName().replace(" ", "_"))
                    .orElseThrow(() -> new NotInDataBaseException("услуг полировки не найдена услуга: ", order.getName().replace("_", " ")));
            ordersPolishing.add(service);
        }
        for (var order : currentOrdersWashing) {
            var service = ordersWashingRepository.findByName(order.getName().replace(" ", "_"))
                    .orElseThrow(() -> new NotInDataBaseException("услуг мойки не найдена услуга: ", order.getName().replace("_", " ")));
            ordersWashing.add(service);
        }
        return (new AllOrderTypes(ordersPolishing, ordersWashing, ordersTire));
    }

}
