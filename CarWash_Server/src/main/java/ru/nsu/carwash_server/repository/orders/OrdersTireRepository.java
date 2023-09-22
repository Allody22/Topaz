package ru.nsu.carwash_server.repository.orders;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.nsu.carwash_server.models.orders.OrdersTire;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface OrdersTireRepository extends JpaRepository<OrdersTire, Long> {
    Optional<OrdersTire> findByName(String name);

    @Modifying
    @Transactional
    @Query(value = "UPDATE orders_tire SET  price_r_13= COALESCE(:PriceR13, price_r_13)," +
            " price_r_14 = COALESCE(:PriceR14, price_r_14), price_r_15 = COALESCE(:PriceR15, price_r_15)," +
            " price_r_16 = COALESCE(:PriceR16,price_r_16),price_r_17 = COALESCE(:PriceR17,price_r_17)," +
            "price_r_18 = COALESCE(:PriceR18,price_r_18), price_r_19 = COALESCE(:PriceR19,price_r_19)," +
            " price_r_20 = COALESCE(:PriceR20,price_r_20), price_r_21 = COALESCE(:PriceR21,price_r_21)," +
            "price_r_22 = COALESCE(:PriceR22,price_r_22), " +
            "time_r_13= COALESCE(:TimeR13, Time_r_13), " +
            "time_r_14 = COALESCE(:TimeR14,time_r_14),time_r_15 = COALESCE(:TimeR15, time_r_15)," +
            "time_r_16 = COALESCE(:TimeR16,time_r_16),time_r_17 = COALESCE(:TimeR17,time_r_17)," +
            "time_r_18 = COALESCE(:TimeR18,time_r_18),time_r_19 = COALESCE(:TimeR19,time_r_19)," +
            "time_r_20 = COALESCE(:TimeR20,time_r_20),time_r_21 = COALESCE(:TimeR21,time_r_21)," +
            "time_r_22 = COALESCE(:TimeR22,time_r_22), " +
            "role = COALESCE(:Role,role)" +
            "WHERE name = :Name", nativeQuery = true)
    void updateTireOrderInfo(@Param("Name") String name, @Param("PriceR13") Integer priceR13, @Param("PriceR14") Integer priceR14,
                             @Param("PriceR15") Integer priceR15, @Param("PriceR16") Integer priceR16, @Param("PriceR17") Integer priceR17,
                             @Param("PriceR18") Integer priceR18, @Param("PriceR19") Integer priceR19,
                             @Param("PriceR20") Integer priceR20, @Param("PriceR21") Integer priceR21,
                             @Param("PriceR22") Integer priceR22,
                             @Param("TimeR13") Integer timeR13, @Param("TimeR14") Integer timeR14,
                             @Param("TimeR15") Integer timeR15, @Param("TimeR16") Integer timeR16,
                             @Param("TimeR17") Integer timeR17, @Param("TimeR18") Integer timeR18,
                             @Param("TimeR19") Integer timeR19, @Param("TimeR20") Integer timeR20,
                             @Param("TimeR21") Integer timeR21, @Param("TimeR22") Integer timeR22,
                             @Param("Role") String role);

    @Transactional
    @Query(value = "SELECT name FROM orders_tire", nativeQuery = true)
    Optional<List<String>> getActualOrders();
}