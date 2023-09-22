package ru.nsu.carwash_server.repository.orders;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.nsu.carwash_server.models.orders.OrdersWashing;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersWashingRepository extends JpaRepository<OrdersWashing, Long> {

    @Modifying
    @Transactional
    @Query(value = "UPDATE orders_washing SET price_first_type = COALESCE(:NewPrice, price_first_type)," +
            " price_second_type = COALESCE(:PriceSecondType, price_second_type), " +
            "price_third_type = COALESCE(:PriceThirdType, price_third_type), time_first_type = COALESCE(:TimeFirstType,time_first_type)," +
            " time_second_type = COALESCE(:TimeSecondType,time_second_type), " +
            "time_third_type = COALESCE(:TimeThirdType,time_third_type), role = COALESCE(:Role,role)" +
            "WHERE name = :Name", nativeQuery = true)
    void updateWashingServiceInfo(@Param("Name") String name, @Param("NewPrice") Integer priceFirst,
                                @Param("PriceSecondType") Integer priceSecond, @Param("PriceThirdType") Integer priceThirdType,
                                @Param("TimeFirstType") Integer timeFirst, @Param("TimeSecondType") Integer timeSecond,
                                @Param("TimeThirdType") Integer timeThird, @Param("Role") String role);

    @Transactional
    @Query(value = "SELECT name FROM orders_washing WHERE role = :Role", nativeQuery = true)
    Optional<List<String>> findAllByRole(@Param("Role") String role);

    @Transactional
    @Query(value = "SELECT * FROM orders_washing WHERE name = :Name ", nativeQuery = true)
    Optional<OrdersWashing> findByName(@Param("Name") String name);

    @Query(value = "SELECT name FROM orders_washing WHERE associated_order LIKE CONCAT('%', :mainOrder, '%')", nativeQuery = true)
    Optional<List<String>> findAllMain(@Param("mainOrder") String mainOrder);

    @Query(value = "SELECT name FROM orders_washing WHERE associated_order LIKE CONCAT('%', :mainOrder, '%') " +
            "AND associated_order NOT LIKE CONCAT('%', :mainOrder, 'Solo%');", nativeQuery = true)
    Optional<List<String>> findAllAssociated(@Param("mainOrder") String mainOrder);
}
