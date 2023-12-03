package ru.nsu.carwash_server.services.interfaces;

import org.springframework.data.util.Pair;
import ru.nsu.carwash_server.models.orders.Order;
import ru.nsu.carwash_server.models.orders.OrderVersions;
import ru.nsu.carwash_server.models.orders.OrdersPolishing;
import ru.nsu.carwash_server.models.orders.OrdersTire;
import ru.nsu.carwash_server.models.orders.OrdersWashing;
import ru.nsu.carwash_server.models.secondary.helpers.AllOrderTypes;
import ru.nsu.carwash_server.models.users.User;
import ru.nsu.carwash_server.payload.request.NewServiceRequest;
import ru.nsu.carwash_server.payload.request.UpdateOrderInfoRequest;
import ru.nsu.carwash_server.payload.request.UpdatePolishingServiceRequest;
import ru.nsu.carwash_server.payload.request.UpdateTireServiceRequest;
import ru.nsu.carwash_server.payload.request.UpdateWashingServiceRequest;
import ru.nsu.carwash_server.payload.response.ConnectedOrdersResponse;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

public interface OrderService {

    /**
     * Обновление информации об услуге мойки
     *
     * @param updateWashingServiceRequest - класс с информацией об обновлении
     * @return описание обновления
     */
    String updateWashingService(UpdateWashingServiceRequest updateWashingServiceRequest);

    /**
     * Обновление информации об услуге шиномонтажа
     *
     * @param updateTireServiceRequest - запрос на обновление с информацией
     * @return описание обновлений
     */
    String updateTireService(UpdateTireServiceRequest updateTireServiceRequest);


    /**
     * Обновление информации об услуге полировки
     *
     * @param updatePolishingServiceRequest - класс с информацией для обновления
     * @return описание обновления
     */
    String updatePolishingService(UpdatePolishingServiceRequest updatePolishingServiceRequest);


    /**
     * Получение всех услуг мойки
     *
     * @param role - необходимая роль услуг
     * @return лист строк из услуг мойки
     */
    List<String> getAllWashingOrdersByRole(String role);

    /**
     * Получаем цену и время определённого списка услуг шиномонтажа.
     *
     * @param orderArray - список услуг
     * @param wheelR     - размер шин
     * @return время и цена заказа. Время первое
     */
    Pair<Integer, Integer> getTireOrderTimePrice(List<String> orderArray, String wheelR);

    /**
     * Получаем цену и время определённого списка услуг полировки.
     *
     * @param orderArray - список услуг
     * @param bodyType   - тип кузова
     * @return время и цена заказа. Время первое
     */
    Pair<Integer, Integer> getPolishingOrderPriceAndTime(List<String> orderArray, int bodyType);


    /**
     * Получаем цену и время определённого списка услуг мойки.
     *
     * @param orderArray - список услуг
     * @param bodyType   - тип кузова
     * @return время и цена заказа. Время первое
     */
    Pair<Integer, Integer> getWashingOrderPriceTime(List<String> orderArray, int bodyType);


    /**
     * Запрос на получение всех услуг мойки
     *
     * @return лист услуг мойки со всей информацией
     */
    List<OrdersWashing> findAllWashingService();

    /**
     * Запрос на получение всех услуг полировки
     *
     * @return лист услуг полировки со всей информацией
     */
    List<OrdersPolishing> findAllPolishingService();

    /**
     * Запрос на получение всех услуг шиномонтажа
     *
     * @return лист услуг шиномонтажа со всей информацией
     */
    List<OrdersTire> findAllTireService();

    /**
     * Получение всех услуг мойки по типу: мойка вип,
     * мойки элит, мойка эконом
     *
     * @param orderName - название типа мойки
     * @return главные в этой мойки и дополнительные к ней услуги
     */
    ConnectedOrdersResponse actualWashingOrders(String orderName);

    /**
     * Создание УСЛУГИ (не заказа) шиномонтажа
     * со всей необходимой информацией
     *
     * @param newServiceRequest - вся информация необходимая для создания услуги шиномонтажа
     * @return сообщение с результатом изменения и информация об услуге
     */
    Pair<String, OrdersTire> createTireService(NewServiceRequest newServiceRequest);


    /**
     * Создание услуги полировки со всей необходимой информацией
     *
     * @param newServiceRequest - вся информация необходимая для создания услуги
     * @return сообщение с результатом изменения и информация об услуге
     */
    Pair<String, OrdersPolishing> createPolishingService(NewServiceRequest newServiceRequest);

    /**
     * Создание УСЛУГИ (не заказа) мойки
     * со всей необходимой информацией
     *
     * @param newServiceRequest - вся информация об услуги необходимая для сохранения
     * @return сообщение с результатом изменения и информация об услуге
     */
    Pair<String, OrdersWashing> createWashingService(NewServiceRequest newServiceRequest);

    /**
     * Обновление заказа, но в нашем случае
     * это создание новой версии конкретного заказа
     *
     * @param updateOrderInfoRequest - запрос со всей информацией о заказе,
     *                               а не представленная информация копируется из
     *                               прошлой версии
     * @return сообщение с описанием обновления
     */
    String updateOrderInfo(UpdateOrderInfoRequest updateOrderInfoRequest);

    /**
     * Удаление заказа, но в нашем случае
     * это создание новой версии конкретного заказа
     * с новым статусом "cancelled"
     *
     * @param orderId - айди заказа для удаления
     */
    void deleteOrder(Long orderId);

    /**
     * Проверка того, что в данном боксе в данное время
     * еще нет заказа
     *
     * @param startTime - начальное время для проверки
     * @param endTime   - конечное время для проверки
     * @param box       - бокс для проверки
     * @return true - если время доступно, false иначе
     */
    boolean checkIfTimeFree(Date startTime, Date endTime, Integer box);


    /**
     * Поиск информации о заказе по айди
     *
     * @param id - айди заказа
     * @return информация о заказе
     */
    Order findById(Long id);

    /**
     * Поиск String имён услуг с аналогичными
     * именами услуг в бд для проверки
     *
     * @param currentOrdersPolishing - текущие услуги полировки
     * @param currentOrdersWashing   - текущие услуги мойки
     * @param currentOrdersTire      - текущие услуги шиномонтажа
     * @return все проверенные услуги
     */
    AllOrderTypes copyOrdersByName(List<OrdersPolishing> currentOrdersPolishing,
                                   List<OrdersWashing> currentOrdersWashing, List<OrdersTire> currentOrdersTire);

    /**
     * Сохранение заказа полировки с сайта или с приложения
     *
     * @param ordersPolishings - набор услуг
     * @param startTime        - начальное время
     * @param endTime          - конечное время
     * @param administrator    - администратор заказа
     * @param specialist       - специалист (мойщик) заказа
     * @param boxNumber        - номер бокса этого заказа
     * @param bonuses          - набор бонусов этого заказа
     * @param comments         - комментарии к заказу
     * @param autoNumber       - номер автомобиля для полировки
     * @param autoType         - тип кузова автомобиля
     * @param userContacts     - контакты пользователя
     * @param user             - сущность пользователя из БД
     * @param price            - цена заказа
     * @param orderSource      - откуда пришёл заказ (сайт или приложение)
     * @param currentStatus    - текущей статус оплаты и готовности заказа
     * @param version          - версия заказа
     * @param sale             - акции к заказу, если она есть
     * @return созданный заказ и его версия в паре
     */
    Pair<Order, OrderVersions> savePolishingOrder(List<OrdersPolishing> ordersPolishings, Date startTime,
                                                  Date endTime, String administrator, String specialist,
                                                  int boxNumber, int bonuses, String comments,
                                                  String autoNumber, int autoType, String userContacts,
                                                  User user, int price, String orderSource,
                                                  String currentStatus, int version, String sale);

    /**
     * Создание заказа шиномонтажа
     *
     * @param ordersTire    - набор услуг
     * @param startTime     - начальное время
     * @param endTime       - конечное время
     * @param administrator - администратор заказа
     * @param specialist    - специалист (мойщик) заказа
     * @param boxNumber     - номер бокса этого заказа
     * @param bonuses       - набор бонусов этого заказа
     * @param comments      - комментарии к заказу
     * @param autoNumber    - номер автомобиля для полировки
     * @param autoType      - тип кузова автомобиля
     * @param userContacts  - контакты пользователя
     * @param user          - сущность пользователя из БД
     * @param price         - цена заказа
     * @param wheelR        - размер колёс автомобиля
     * @param orderSource   - откуда пришёл заказ (сайт или приложение)
     * @param currentStatus - текущей статус оплаты и готовности заказа
     * @param version       - версия заказа
     * @param sale          - акции к заказу, если она есть
     * @return созданный заказ и его версия в паре
     */
    Pair<Order, OrderVersions> saveTireOrder(List<OrdersTire> ordersTire, Date startTime, Date endTime,
                                             String administrator, String specialist, int boxNumber,
                                             int bonuses, String comments, String autoNumber,
                                             int autoType, String userContacts, User user,
                                             int price, String wheelR, String orderSource,
                                             String currentStatus, int version, String sale);

    /**
     * Создание заказа мойки
     *
     * @param ordersWashings - набор услуг
     * @param startTime      - начальное время
     * @param endTime        - конечное время
     * @param administrator  - администратор заказа
     * @param specialist     - специалист (мойщик) заказа
     * @param boxNumber      - номер бокса этого заказа
     * @param bonuses        - набор бонусов этого заказа
     * @param comments       - комментарии к заказу
     * @param autoNumber     - номер автомобиля для полировки
     * @param autoType       - тип кузова автомобиля
     * @param userContacts   - контакты пользователя
     * @param user           - сущность пользователя из БД
     * @param price          - цена заказа
     * @param currentStatus  - текущей статус оплаты и готовности заказа
     * @param version        - версия заказа
     * @param sale           - акции к заказу, если она есть
     * @return созданный заказ и его версия в паре
     */
    Pair<Order, OrderVersions> saveWashingOrder(List<OrdersWashing> ordersWashings, Date startTime,
                                                Date endTime, String administrator, String specialist,
                                                int boxNumber, int bonuses, String comments,
                                                String autoNumber, int autoType, String userContacts,
                                                User user, int price, String orderType,
                                                String currentStatus, int version, String sale);

    /**
     * Получение всех заказов в заданный
     * интервал времени
     *
     * @param startTime - начальное время
     * @param endTime   - конечное время
     * @param box       - номер бокса
     * @param flag      - если передано true - то учитываем отменённые заказа
     * @return набор всех последних версий заказов в этом интервале
     */
    List<OrderVersions> getOrdersInTimeInterval(Date startTime, Date endTime, Integer box, boolean flag);

    /**
     * Получение заказов, созданных в этот интервал времени
     *
     * @param firstDate  - начальная дата
     * @param secondDate - конечная дата
     * @param flag       - если передано true - то учитываем отменённые заказа
     * @return набор всех последних версий заказов
     */
    List<OrderVersions> getOrdersCreatedAt(Date firstDate, Date secondDate, boolean flag);

    /**
     * Получение всех последних версий заказов, которые были созданы
     *
     * @return список из всех заказов
     */
    List<OrderVersions> getAllOrders();

    /**
     * Получение всех не сделанных заказов
     *
     * @param flag - если передано true - то учитываем отменённые заказа
     * @return набор всех последних версий заказов
     */
    List<OrderVersions> getNotMadeOrders(boolean flag);


    /**
     * Получение всех заказов определённого пользователя за определённое время.
     *
     * @param firstDate  - время начала
     * @param secondDate - время конца
     * @param userId     - айди пользователя
     * @return - список всех заказов
     */
    List<Order> getUserOrdersInTimeInterval(Date firstDate, Date secondDate, Long userId);

    /**
     * Получение последний версии данного заказа
     *
     * @param id - айди заказа
     * @return последняя версия нужного заказа
     */
    OrderVersions getActualOrderVersion(@NotNull Long id);
}
