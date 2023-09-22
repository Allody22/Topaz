package ru.nsu.carwash_server.repository.orders;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.carwash_server.models.orders.Order;

@Repository
public interface OrdersRepository extends JpaRepository<Order, Long> {
}
