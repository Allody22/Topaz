package ru.nsu.carwash_server.repository.operations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.nsu.carwash_server.models.operations.OperationsUserLink;
import ru.nsu.carwash_server.models.operations.OperationsVersions;
import ru.nsu.carwash_server.models.users.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OperationsUsersLinkRepository extends JpaRepository<OperationsUserLink, Long> {

    @Query(value = "SELECT * FROM operations_users_link WHERE user_id = :UserId", nativeQuery = true)
    Optional<User> findByUserId(@Param("UserId") Long userId);

    @Query(value = "SELECT * FROM operations_users_link WHERE description LIKE %:phoneNumber% " +
            "AND description LIKE %:advice% AND creation_time >= :startTime " +
            "ORDER BY creation_time DESC LIMIT 1", nativeQuery = true)
    Optional<OperationsUserLink> findLatestByDescriptionContainingWithAdviceInLastHour(@Param("phoneNumber") String phoneNumber,
                                                                                       @Param("advice") String advice,
                                                                                       @Param("startTime") LocalDateTime startTime);

    @Query(value = "SELECT * FROM operations_users_link WHERE description " +
            "LIKE %:phoneNumber% AND description LIKE %:advice% AND creation_time > :threshold " +
            "ORDER BY creation_time DESC", nativeQuery = true)
    List<OperationsUserLink> findAllByDescriptionContainingWithAdvice(@Param("phoneNumber") String phoneNumber,
                                                                      @Param("advice") String advice,
                                                                      @Param("threshold") LocalDateTime threshold);


    @Query(value = "SELECT * FROM operations_users_link WHERE description LIKE %:phoneNumber%", nativeQuery = true)
    List<OperationsUserLink> findByDescriptionContaining(@Param("phoneNumber") String phoneNumber);

    @Query(value = "SELECT * FROM operations_users_link WHERE operation_id = :OperationId", nativeQuery = true)
    Optional<User> findByOperation_Id(@Param("OperationId") Long operationId);


    @Query("SELECT oul.operation FROM OperationsUserLink oul WHERE oul.user.id = :userId")
    List<OperationsVersions> findAllOperationsByUserId(@Param("userId") Long userId);

    @Query("SELECT oul.operation FROM OperationsUserLink oul WHERE oul.user.id = :userId")
    List<OperationsVersions> findAllOperationsByUserUser(@Param("userId") Long userId);

}
