package ru.nsu.carwash_server.models.secondary.helpers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.nsu.carwash_server.models.orders.OrdersPolishing;
import ru.nsu.carwash_server.models.orders.OrdersTire;
import ru.nsu.carwash_server.models.orders.OrdersWashing;

import java.util.List;

@AllArgsConstructor
@Getter
public class AllOrderTypes {
    List<OrdersPolishing> ordersPolishing;
    List<OrdersWashing> ordersWashing;
    List<OrdersTire> ordersTire;
}
