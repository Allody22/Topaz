package ru.nsu.carwash_server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import ru.nsu.carwash_server.models.orders.Order;
import ru.nsu.carwash_server.models.orders.OrderVersions;
import ru.nsu.carwash_server.models.orders.OrdersPolishing;
import ru.nsu.carwash_server.models.orders.OrdersTire;
import ru.nsu.carwash_server.models.orders.OrdersWashing;
import ru.nsu.carwash_server.models.users.User;
import ru.nsu.carwash_server.models.secondary.constants.OrderStatuses;
import ru.nsu.carwash_server.models.secondary.exception.NotInDataBaseException;
import ru.nsu.carwash_server.models.secondary.helpers.AllOrderTypes;
import ru.nsu.carwash_server.payload.request.UpdateOrderInfoRequest;
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

    public List<OrdersWashing> findAllWashingService() {
        return ordersWashingRepository.findAll();
    }

    public List<OrdersPolishing> findAllPolishingService() {
        return ordersPolishingRepository.findAll();
    }

    public List<OrdersTire> findAllTireService() {
        return ordersTireRepository.findAll();
    }

    public ConnectedOrdersResponse actualWashingOrders(String orderName) {
        List<String> mainOrders = ordersWashingRepository.findAllMain(orderName + "Solo")
                .orElse(null);
        List<String> connectedOrders = ordersWashingRepository.findAllAssociated(orderName)
                .orElse(null);
        return new ConnectedOrdersResponse(mainOrders, connectedOrders);
    }

    public OrdersTire createTireService(OrdersTire ordersTire) {
        return ordersTireRepository.save(ordersTire);
    }

    public OrdersPolishing createPolishingService(OrdersPolishing ordersPolishing) {
        return ordersPolishingRepository.save(ordersPolishing);
    }

    public OrdersWashing createWashingService(OrdersWashing ordersWashing) {
        return ordersWashingRepository.save(ordersWashing);
    }

    @Transactional
    public Pair<Boolean, String> updateOrderInfo(UpdateOrderInfoRequest updateOrderInfoRequest) {
        Pair<Boolean, String> result = Pair.of(false, "");
        var orderId = updateOrderInfoRequest.getOrderId();


        Optional<Order> optionalOrder = ordersRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();

            var actualOrderVersion = getActualOrderVersion(orderId);

            List<String> ordersList = updateOrderInfoRequest.getOrders();

            List<OrdersWashing> ordersWashings = new ArrayList<>();
            List<OrdersPolishing> ordersPolishings = new ArrayList<>();
            List<OrdersTire> ordersTires = new ArrayList<>();
            if (updateOrderInfoRequest.getOrderType().contains("tire")) {
                for (var tireOrder : ordersList) {
                    var service = ordersTireRepository.findByName(tireOrder.replace(" ", "_"))
                            .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", tireOrder.replace("_", " ")));
                    ordersTires.add(service);
                }
            } else if (updateOrderInfoRequest.getOrderType().contains("polish")) {
                for (var polishOrder : ordersList) {
                    var service = ordersPolishingRepository.findByName(polishOrder.replace(" ", "_"))
                            .orElseThrow(() -> new NotInDataBaseException("услуг полировки не найдена услуга: ", polishOrder.replace("_", " ")));
                    ordersPolishings.add(service);
                }
            } else if (updateOrderInfoRequest.getOrderType().contains("wash")) {
                for (var washingOrders : ordersList) {
                    var service = ordersWashingRepository.findByName(washingOrders.replace(" ", "_"))
                            .orElseThrow(() -> new NotInDataBaseException("услуг мойки не найдена услуга: ", washingOrders.replace("_", " ")));
                    ordersWashings.add(service);
                }
            } else {
                String resultText = ("Типа заказа:'" +
                        updateOrderInfoRequest.getOrderType() + "' не существует");
                result = Pair.of(false, resultText);
                return result;
            }

            AllOrderTypes allOrdersByTypes = copyOrdersByName(actualOrderVersion.getOrdersPolishings(),
                    actualOrderVersion.getOrdersWashing(), actualOrderVersion.getOrdersTires());

            OrderVersions newOrderVersion = new OrderVersions(actualOrderVersion, updateOrderInfoRequest,
                    ordersTires, ordersPolishings, ordersWashings, allOrdersByTypes.getOrdersTire(),
                    allOrdersByTypes.getOrdersPolishing(), allOrdersByTypes.getOrdersWashing());
            result = Pair.of(true, "Информация о заказе успешно обновлена");

            order.addOrderVersion(newOrderVersion);

            return result;
        } else {
            return Pair.of(false, "Такого заказа не существует");
        }
    }

    @Transactional
    public Pair<Boolean, String> deleteOrder(Long orderId) {
        Pair<Boolean, String> result = Pair.of(false, "");

        Optional<Order> optionalOrder = ordersRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();

            var actualOrderVersion = getActualOrderVersion(orderId);

            List<OrdersWashing> ordersWashings = new ArrayList<>();
            List<OrdersPolishing> ordersPolishings = new ArrayList<>();
            List<OrdersTire> ordersTires = new ArrayList<>();
            if (actualOrderVersion.getOrderType().contains("tire")) {
                for (var tireOrder : actualOrderVersion.getOrdersTires()) {
                    var service = ordersTireRepository.findByName(tireOrder.getName())
                            .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга: ", tireOrder.getName().replace("_", " ")));
                    ordersTires.add(service);
                }
            } else if (actualOrderVersion.getOrderType().contains("polish")) {
                for (var polishOrder : actualOrderVersion.getOrdersPolishings()) {
                    var service = ordersPolishingRepository.findByName(polishOrder.getName().replace(" ", "_"))
                            .orElseThrow(() -> new NotInDataBaseException("услуг полировки не найдена услуга: ", polishOrder.getName().replace("_", " ")));
                    ordersPolishings.add(service);
                }
            } else if (actualOrderVersion.getOrderType().contains("wash")) {
                for (var washingOrders : actualOrderVersion.getOrdersWashing()) {
                    var service = ordersWashingRepository.findByName(washingOrders.getName().replace(" ", "_"))
                            .orElseThrow(() -> new NotInDataBaseException("услуг мойки не найдена услуга: ", washingOrders.getName().replace("_", " ")));
                    ordersWashings.add(service);
                }
            } else {
                String resultText = ("Типа заказа:'" +
                        actualOrderVersion.getOrderType() + "' не существует");
                result = Pair.of(false, resultText);
                return result;
            }

            AllOrderTypes allOrdersByTypes = copyOrdersByName(actualOrderVersion.getOrdersPolishings(),
                    actualOrderVersion.getOrdersWashing(), actualOrderVersion.getOrdersTires());

            UpdateOrderInfoRequest updateOrderInfoRequest = new UpdateOrderInfoRequest();
            updateOrderInfoRequest.setCurrentStatus(OrderStatuses.cancelled);
            OrderVersions newOrderVersion = new OrderVersions(actualOrderVersion,updateOrderInfoRequest,
                    ordersTires, ordersPolishings, ordersWashings, allOrdersByTypes.getOrdersTire(),
                    allOrdersByTypes.getOrdersPolishing(), allOrdersByTypes.getOrdersWashing());
            result = Pair.of(true, "Информация о заказе успешно обновлена");

            order.addOrderVersion(newOrderVersion);

            return result;
        } else {
            return Pair.of(false, "Такого заказа не существует");
        }
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
            if (flag){
                return orderVersionsRepository.getLatestOrderVersionsInOneDayFullWithCancelled(startTime, endTime);
            }
            return orderVersionsRepository.getLatestOrderVersionsInOneDayFull(startTime, endTime);
        } else {
            if (flag){
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
