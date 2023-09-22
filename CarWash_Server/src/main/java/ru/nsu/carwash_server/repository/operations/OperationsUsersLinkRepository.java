package ru.nsu.carwash_server.repository.operations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.nsu.carwash_server.models.OperationsUserLink;
import ru.nsu.carwash_server.models.OperationsVersions;
import ru.nsu.carwash_server.models.users.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperationsUsersLinkRepository extends JpaRepository<OperationsUserLink, Long> {

    @Query(value = "SELECT * FROM operations_users_link WHERE user_id = :UserId", nativeQuery = true)
    Optional<User> findByUserId(@Param("UserId") Long userId);


    @Query(value = "SELECT * FROM operations_users_link WHERE operation_id = :OperationId", nativeQuery = true)
    Optional<User> findByOperation_Id(@Param("OperationId") Long operationId);


    @Query("SELECT oul.operation FROM OperationsUserLink oul WHERE oul.user.id = :userId")
    List<OperationsVersions> findAllOperationsByUserId(@Param("userId") Long userId);

    @Query("SELECT oul.operation FROM OperationsUserLink oul WHERE oul.user.id = :userId")
    List<OperationsVersions> findAllOperationsByUserUser(@Param("userId") Long userId);

}
