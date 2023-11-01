package ru.nsu.carwash_server.services;

import org.springframework.stereotype.Service;
import ru.nsu.carwash_server.models.orders.OrderVersions;
import ru.nsu.carwash_server.models.orders.OrdersPolishing;
import ru.nsu.carwash_server.models.orders.OrdersTire;
import ru.nsu.carwash_server.models.orders.OrdersWashing;
import ru.nsu.carwash_server.models.secondary.constants.OrderStatuses;
import ru.nsu.carwash_server.models.users.UserVersions;
import ru.nsu.carwash_server.payload.request.UpdateOrderInfoRequest;
import ru.nsu.carwash_server.payload.request.UpdateUserInfoRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class OperationsDescriptionService {


    public String getWashingOrderChangingInfo(Integer priceFirstType, Integer priceSecondType, Integer priceThirdType,
                                              Integer timeFirstType, Integer timeSecondType, Integer timeThirdType,
                                              String context, String orderName, OrdersWashing ordersWashing) {

        StringBuilder sb = new StringBuilder();

        sb.append(context).append(" '").append(orderName.replace("_", " ")).append("', получившая");

        if (priceFirstType != null && (priceFirstType != ordersWashing.getPriceFirstType())) {
            sb.append(" цену за 1 тип: '").append(priceFirstType).append("',");
        }
        if (priceSecondType != null && (priceSecondType != ordersWashing.getPriceSecondType())) {
            sb.append(" цену за 2 тип: '").append(priceSecondType).append("',");
        }
        if (priceThirdType != null && (priceThirdType != ordersWashing.getPriceThirdType())) {
            sb.append(" цену за 3 тип: '").append(priceThirdType).append("',");
        }
        if (timeFirstType != null && (timeFirstType != ordersWashing.getTimeFirstType())) {
            sb.append(" время за 1 тип: '").append(timeFirstType).append("',");
        }
        if (timeSecondType != null && (timeSecondType != ordersWashing.getTimeSecondType())) {
            sb.append(" время за 2 тип: '").append(timeSecondType).append("',");
        }
        if (timeThirdType != null && (timeThirdType != ordersWashing.getTimeThirdType())) {
            sb.append(" время за 3 тип: '").append(timeThirdType).append("',");
        }

        // Удалить последнюю запятую, если она есть
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }

    public String getPolishingOrderChangingInfo(Integer priceFirstType, Integer priceSecondType, Integer priceThirdType,
                                                Integer timeFirstType, Integer timeSecondType, Integer timeThirdType,
                                                String context, String orderName, OrdersPolishing ordersPolishing) {

        StringBuilder sb = new StringBuilder();

        sb.append(context).append(" '").append(orderName.replace("_", " ")).append("', получившая");

        if (priceFirstType != null && (priceFirstType != ordersPolishing.getPriceFirstType())) {
            sb.append(" цену за 1 тип: '").append(priceFirstType).append("',");
        }
        if (priceSecondType != null && (priceSecondType != ordersPolishing.getPriceSecondType())) {
            sb.append(" цену за 2 тип: '").append(priceSecondType).append("',");
        }
        if (priceThirdType != null && (priceThirdType != ordersPolishing.getPriceThirdType())) {
            sb.append(" цену за 3 тип: '").append(priceThirdType).append("',");
        }
        if (timeFirstType != null && (timeFirstType != ordersPolishing.getTimeFirstType())) {
            sb.append(" время за 1 тип: '").append(timeFirstType).append("',");
        }
        if (timeSecondType != null && (timeSecondType != ordersPolishing.getTimeSecondType())) {
            sb.append(" время за 2 тип: '").append(timeSecondType).append("',");
        }
        if (timeThirdType != null && (timeThirdType != ordersPolishing.getTimeThirdType())) {
            sb.append(" время за 3 тип: '").append(timeThirdType).append("',");
        }

        // Удалить последнюю запятую, если она есть
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }

    public String getUpdateOrderDescription(UpdateOrderInfoRequest updateOrderInfoRequest, OrderVersions actualOrderVersion) {
        StringBuilder descriptionBuilder = new StringBuilder();

        List<String> newOrders = updateOrderInfoRequest.getOrders();
        descriptionBuilder.append("Заказ с айди '").append(updateOrderInfoRequest.getOrderId()).append("' получил ");

        List<String> parts = new ArrayList<>();

        if (updateOrderInfoRequest.getWheelR() != null && !(actualOrderVersion.getWheelR().equals(updateOrderInfoRequest.getWheelR()))) {
            parts.add("новый размер шин: '" + updateOrderInfoRequest.getWheelR() + "'");
        }
        if (updateOrderInfoRequest.getUserPhone() != null && !(actualOrderVersion.getUserContacts().equals(updateOrderInfoRequest.getUserPhone()))) {
            parts.add("новый контакт клиента: '" + updateOrderInfoRequest.getUserPhone() + "'");
        }
        if (updateOrderInfoRequest.getOrderType() != null && !(actualOrderVersion.getOrderType().equals(updateOrderInfoRequest.getOrderType()))) {
            parts.add("новый тип заказа: '" + updateOrderInfoRequest.getOrderType() + "'");
        }
        if (updateOrderInfoRequest.getPrice() != null && (actualOrderVersion.getPrice() != (updateOrderInfoRequest.getPrice()))) {
            parts.add("новую цену: '" + updateOrderInfoRequest.getPrice() + "'");
        }
        if (updateOrderInfoRequest.getStartTime() != null && (!actualOrderVersion.getStartTime().equals(updateOrderInfoRequest.getStartTime()))) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            String formattedStartTime = sdf.format(updateOrderInfoRequest.getStartTime());
            parts.add("новое время начала: '" + formattedStartTime + "'");
        }
        if (updateOrderInfoRequest.getEndTime() != null && !actualOrderVersion.getEndTime().equals(updateOrderInfoRequest.getEndTime())) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            String formattedEndTime = sdf.format(updateOrderInfoRequest.getEndTime());
            parts.add("новое время конца: '" + formattedEndTime + "'");
        }
        if (updateOrderInfoRequest.getAdministrator() != null && !(actualOrderVersion.getAdministrator().equals(updateOrderInfoRequest.getAdministrator()))) {
            parts.add("нового администратора: '" + updateOrderInfoRequest.getAdministrator() + "'");
        }
        if (updateOrderInfoRequest.getAutoNumber() != null && !(actualOrderVersion.getAutoNumber().equals(updateOrderInfoRequest.getAutoNumber()))) {
            parts.add("новый номер авто: '" + updateOrderInfoRequest.getAutoNumber() + "'");
        }
        if (updateOrderInfoRequest.getAutoType() != null && actualOrderVersion.getAutoType() != updateOrderInfoRequest.getAutoType()) {
            parts.add("новый тип авто: '" + updateOrderInfoRequest.getAutoType() + "'");
        }
        if (updateOrderInfoRequest.getSpecialist() != null && !(actualOrderVersion.getSpecialist().equals(updateOrderInfoRequest.getSpecialist()))) {
            parts.add("нового специалиста: '" + updateOrderInfoRequest.getSpecialist() + "'");
        }
        if (updateOrderInfoRequest.getBoxNumber() != null && (actualOrderVersion.getBoxNumber() != updateOrderInfoRequest.getBoxNumber())) {
            parts.add("новый бокс: '" + updateOrderInfoRequest.getBoxNumber() + "'");
        }
        if (updateOrderInfoRequest.getComments() != null && !(actualOrderVersion.getComments().equals(updateOrderInfoRequest.getComments()))) {
            parts.add("новые комментарии: '" + updateOrderInfoRequest.getComments() + "'");
        }
        if (newOrders != null) {
            List<String> currentOrdersList = getOrderNamesOnly(actualOrderVersion.getOrdersPolishings(), actualOrderVersion.getOrdersWashing(),
                    actualOrderVersion.getOrdersTires());

            List<String> addedServices = new ArrayList<>(newOrders);
            addedServices.removeAll(currentOrdersList);  // Удаляем из новых заказов все текущие, оставляем только добавленные

            List<String> removedServices = new ArrayList<>(currentOrdersList);
            removedServices.removeAll(newOrders);  // Удаляем из текущих заказов все новые, оставляем только удаленные

            if (!addedServices.isEmpty()) {
                parts.add("Добавленные услуги: '" + String.join(", ", addedServices) + "'");
            }
            if (!removedServices.isEmpty()) {
                parts.add("Удаленные услуги: '" + String.join(", ", removedServices) + "'");
            }
        }
        if (updateOrderInfoRequest.getCurrentStatus() != null && !(actualOrderVersion.getCurrentStatus().equals(updateOrderInfoRequest.getCurrentStatus()))) {
            parts.add("новое состояние: '" + OrderStatuses.getTranslatedStatus(updateOrderInfoRequest.getCurrentStatus()) + "'");
        }

        String combined = String.join(", ", parts);

        descriptionBuilder.append(combined);
        return descriptionBuilder.toString();
    }

    public String getPolishingWashingOrderChangingInfo(Integer priceFirstType, Integer priceSecondType, Integer priceThirdType,
                                                       Integer timeFirstType, Integer timeSecondType, Integer timeThirdType,
                                                       String context, String orderName) {

        StringBuilder sb = new StringBuilder();

        sb.append(context).append(" '").append(orderName.replace("_", " ")).append("', получившая");

        if (priceFirstType != null) {
            sb.append(" цену за 1 тип: '").append(priceFirstType).append("',");
        }
        if (priceSecondType != null) {
            sb.append(" цену за 2 тип: '").append(priceSecondType).append("',");
        }
        if (priceThirdType != null) {
            sb.append(" цену за 3 тип: '").append(priceThirdType).append("',");
        }
        if (timeFirstType != null) {
            sb.append(" время за 1 тип: '").append(timeFirstType).append("',");
        }
        if (timeSecondType != null) {
            sb.append(" время за 2 тип: '").append(timeSecondType).append("',");
        }
        if (timeThirdType != null) {
            sb.append(" время за 3 тип: '").append(timeThirdType).append("',");
        }

        // Удалить последнюю запятую, если она есть
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }

    public String updateUserDescription(UpdateUserInfoRequest updateUserInfoRequest, String username, UserVersions previousUserVersion) {
        StringBuilder message = new StringBuilder();

        message.append("Пользователь '").append(username).append("' получил");

        if (updateUserInfoRequest.getPhone() != null && !previousUserVersion.getPhone().equals(updateUserInfoRequest.getPhone())) {
            message.append(" новый телефон: '").append(updateUserInfoRequest.getPhone()).append("',");
        }

        if (updateUserInfoRequest.getFullName() != null && !previousUserVersion.getFullName().equals(updateUserInfoRequest.getFullName())) {
            message.append(" новое ФИО: '").append(updateUserInfoRequest.getFullName()).append("',");
        }

        if (updateUserInfoRequest.getAdminNote() != null && !updateUserInfoRequest.getAdminNote().equals(previousUserVersion.getAdminNote())) {
            message.append(" новую заметку от администратора: '").append(updateUserInfoRequest.getAdminNote()).append("',");
        }

        if (updateUserInfoRequest.getEmail() != null && !updateUserInfoRequest.getEmail().equals(previousUserVersion.getEmail())) {
            message.append(" новую почту: '").append(updateUserInfoRequest.getEmail()).append("',");
        }

        if (message.charAt(message.length() - 1) == ',') {
            message.deleteCharAt(message.length() - 1);
        }

        return message.toString();
    }


    public List<String> getOrderNamesOnly(List<OrdersPolishing> currentOrdersPolishing,
                                          List<OrdersWashing> currentOrdersWashing, List<OrdersTire> currentOrdersTire) {

        List<String> orderNames = new ArrayList<>();

        for (var order : currentOrdersTire) {
            orderNames.add(order.getName());
        }
        for (var order : currentOrdersWashing) {
            orderNames.add(order.getName());
        }
        for (var order : currentOrdersPolishing) {
            orderNames.add(order.getName());
        }
        return (orderNames);
    }
}
