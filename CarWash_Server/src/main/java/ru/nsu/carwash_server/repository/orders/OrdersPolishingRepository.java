package ru.nsu.carwash_server.repository.orders;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.nsu.carwash_server.models.orders.OrdersPolishing;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersPolishingRepository extends JpaRepository<OrdersPolishing, Long> {
    Optional<OrdersPolishing> findByName(String name);

    @Modifying
    @Transactional
    @Query(value = "UPDATE orders_polishing SET price_first_type = COALESCE(:NewPrice, price_first_type)," +
            " price_second_type = COALESCE(:PriceSecondType, price_second_type), " +
            "price_third_type = COALESCE(:PriceThirdType, price_third_type), time_first_type = COALESCE(:TimeFirstType,time_first_type)," +
            " time_second_type = COALESCE(:TimeSecondType,time_second_type), " +
            "time_third_type = COALESCE(:TimeThirdType,time_third_type)" +
            "WHERE name = :Name", nativeQuery = true)
    void updatePolishingOrder(@Param("Name") String name, @Param("NewPrice") Integer priceFirst,
                                @Param("PriceSecondType") Integer priceSecond, @Param("PriceThirdType") Integer priceThirdType,
                                @Param("TimeFirstType") Integer timeFirst, @Param("TimeSecondType") Integer timeSecond,
                                @Param("TimeThirdType") Integer timeThird);

    @Transactional
    @Query(value = "SELECT name FROM orders_polishing",nativeQuery = true)
    Optional<List<String>> getActualOrders();
}