package ru.nsu.carwash_server.repository.orders;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.nsu.carwash_server.models.orders.OrderVersions;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface OrderVersionsRepository extends JpaRepository<OrderVersions, Long> {

    @Query(value = "SELECT * FROM orders_versions WHERE order_id = :OrderId ORDER BY creation_time DESC LIMIT 1", nativeQuery = true)
    Optional<OrderVersions> findLatestVersionByOrderId(@Param("OrderId") Long orderId);

    @Query(value =
            "SELECT o.* FROM orders_versions o INNER JOIN ( " +
                    "SELECT order_id, MAX(creation_time) as maxDate " +
                    "FROM orders_versions GROUP BY order_id ) as subquery " +
                    "ON o.order_id = subquery.order_id AND o.creation_time = subquery.maxDate " +
                    "WHERE (o.box_number = :Box AND o.current_status NOT LIKE '%cancelled%' " +
                    "AND ((o.start_time BETWEEN :StartTime AND :EndTime) OR (o.end_time BETWEEN :StartTime AND :EndTime))) ",
            nativeQuery = true)
    List<OrderVersions> getLatestOrderVersionsInOneDayFullInBox(@Param("StartTime") Date startTime,
                                                                @Param("EndTime") Date endTime, @Param("Box") int boxNumber);

    @Query(value =
            "SELECT o.* FROM orders_versions o INNER JOIN ( " +
                    "SELECT order_id, MAX(creation_time) as maxDate " +
                    "FROM orders_versions GROUP BY order_id ) as subquery " +
                    "ON o.order_id = subquery.order_id AND o.creation_time = subquery.maxDate " +
                    "WHERE ((o.box_number = :Box) AND " +
                    " ((o.start_time BETWEEN :StartTime AND :EndTime) OR (o.end_time BETWEEN :StartTime AND :EndTime)))",
            nativeQuery = true)
    List<OrderVersions> getLatestOrderVersionsInOneDayFullInBoxWithCancelled(@Param("StartTime") Date startTime,
                                                                             @Param("EndTime") Date endTime, @Param("Box") int boxNumber);

    @Query(value =
            "SELECT o.* FROM orders_versions o INNER JOIN ( " +
                    "SELECT order_id, MAX(creation_time) as maxDate " +
                    "FROM orders_versions GROUP BY order_id ) as subquery " +
                    "ON o.order_id = subquery.order_id AND o.creation_time = subquery.maxDate " +
                    "WHERE (o.current_status NOT LIKE '%cancelled%' AND (o.start_time BETWEEN :StartTime AND :EndTime " +
                    "OR o.end_time BETWEEN :StartTime AND :EndTime))",
            nativeQuery = true)
    List<OrderVersions> getLatestOrderVersionsInOneDayFull(@Param("StartTime") Date startTime,
                                                           @Param("EndTime") Date endTime);

    @Query(value =
            "SELECT o.* FROM orders_versions o INNER JOIN ( " +
                    "SELECT order_id, MAX(creation_time) as maxDate " +
                    "FROM orders_versions GROUP BY order_id ) as subquery " +
                    "ON o.order_id = subquery.order_id AND o.creation_time = subquery.maxDate " +
                    "WHERE (o.start_time BETWEEN :StartTime AND :EndTime OR o.end_time BETWEEN :StartTime AND :EndTime)",
            nativeQuery = true)
    List<OrderVersions> getLatestOrderVersionsInOneDayFullWithCancelled(@Param("StartTime") Date startTime,
                                                                        @Param("EndTime") Date endTime);

    @Query(value =
            "SELECT o.* FROM orders_versions o INNER JOIN ( " +
                    "SELECT order_id, MAX(creation_time) as maxDate " +
                    "FROM orders_versions GROUP BY order_id ) as subquery " +
                    "ON o.order_id = subquery.order_id AND o.creation_time = subquery.maxDate " +
                    "WHERE ((o.creation_time BETWEEN :StartTime AND :EndTime)  AND o.current_status NOT LIKE '%cancelled%')",
            nativeQuery = true)
    List<OrderVersions> getLatestVersionByDateOfCreation(@Param("StartTime") Date startTime,
                                                         @Param("EndTime") Date endTime);

    @Query(value =
            "SELECT o.* FROM orders_versions o INNER JOIN ( " +
                    "SELECT order_id, MAX(creation_time) as maxDate " +
                    "FROM orders_versions GROUP BY order_id ) as subquery " +
                    "ON o.order_id = subquery.order_id AND o.creation_time = subquery.maxDate " +
                    "WHERE (o.creation_time BETWEEN :StartTime AND :EndTime)",
            nativeQuery = true)
    List<OrderVersions> getLatestVersionByDateOfCreationWithCancelled(@Param("StartTime") Date startTime,
                                                                      @Param("EndTime") Date endTime);

    @Query(value =
            "SELECT o.* FROM orders_versions o INNER JOIN ( " +
                    "SELECT order_id, MAX(creation_time) as maxDate " +
                    "FROM orders_versions GROUP BY order_id ) as subquery " +
                    "ON o.order_id = subquery.order_id AND o.creation_time = subquery.maxDate",
            nativeQuery = true)
    List<OrderVersions> getLatestVersion();

    @Query(value =
            "SELECT o.* FROM orders_versions o INNER JOIN ( " +
                    "SELECT order_id, MAX(creation_time) as maxDate " +
                    "FROM orders_versions GROUP BY order_id ) as subquery " +
                    "ON o.order_id = subquery.order_id AND o.creation_time = subquery.maxDate " +
                    "WHERE (o.box_number = :Box AND o.current_status NOT LIKE '%cancelled%' " +
                    "AND (( :StartTime >= o.start_time AND :EndTime <= o.end_time) " +
                    "OR (:StartTime <= o.start_time AND :EndTime > o.start_time) " +
                    "OR (:StartTime < o.end_time AND :EndTime >= o.end_time)))",
            nativeQuery = true)
    List<OrderVersions> getOrderLatestVersionAroundThisTimeInterval(@Param("StartTime") Date startTime,
                                                                    @Param("EndTime") Date endTime, @Param("Box") int boxNumber);


    @Query(value = "WITH RankedOrders AS ( SELECT ov.*, " +
            "ROW_NUMBER() OVER(PARTITION BY ov.order_id ORDER BY ov.version DESC)" +
            " AS rn FROM orders_versions ov WHERE ov.box_number = :Box " +
            "AND (( :StartTime >= ov.start_time AND :EndTime <= ov.end_time) " +
            "OR (:StartTime <= ov.start_time AND :EndTime > ov.start_time) " +
            "OR (:StartTime < ov.end_time AND ov.end_time <= :EndTime))) " +
            "SELECT * FROM RankedOrders WHERE rn = 1",
            nativeQuery = true)
    List<OrderVersions> getOrderLatestVersionAroundThisTimeIntervalWithCancelled(@Param("StartTime") Date startTime,
                                                                                 @Param("EndTime") Date endTime, @Param("Box") int boxNumber);


    @Query(value = "SELECT * FROM orders_versions WHERE order_id = :OrderId AND creation_time = (SELECT MAX(creation_time)" +
            " FROM orders_versions WHERE order_id = :OrderId) AND current_status LIKE '%NotDone%'", nativeQuery = true)
    Optional<OrderVersions> findLatestVersionIfNotDoneByOrderId(@Param("OrderId") Long orderId);

    @Query(value =
            "SELECT o.* FROM orders_versions o INNER JOIN ( " +
                    "SELECT order_id, MAX(creation_time) as maxDate " +
                    "FROM orders_versions GROUP BY order_id ) as subquery " +
                    "ON o.order_id = subquery.order_id AND o.creation_time = subquery.maxDate " +
                    "WHERE o.current_status LIKE '%NotDone%'",
            nativeQuery = true)
    List<OrderVersions> getLatestOrderVersionsWithStatusNotDone();

    @Query(value =
            "SELECT o.* FROM orders_versions o INNER JOIN ( " +
                    "SELECT order_id, MAX(creation_time) as maxDate " +
                    "FROM orders_versions GROUP BY order_id ) as subquery " +
                    "ON o.order_id = subquery.order_id AND o.creation_time = subquery.maxDate " +
                    "WHERE (o.current_status LIKE '%NotDone%'  OR o.current_status LIKE '%cancelled%')",
            nativeQuery = true)
    List<OrderVersions> getLatestOrderVersionsWithStatusNotDoneWithCancelled();


}