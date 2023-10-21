package ru.nsu.carwash_server.repository.orders;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.nsu.carwash_server.models.orders.Order;

import java.util.Date;
import java.util.List;

@Repository
public interface OrdersRepository extends JpaRepository<Order, Long> {

    @Query(value =
            "SELECT * FROM orders WHERE ((creation_time BETWEEN :startTime AND :endTime) AND user_id = :userId)",
            nativeQuery = true)
    List<Order> findAllByDateOfCreationAndUser(@Param("startTime") Date startTime, @Param("endTime") Date endTime,
                                               @Param("userId") Long UserId);
}
