package ru.nsu.carwash_server.repository.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.nsu.carwash_server.models.users.UserVersions;

import java.util.List;

public interface UserVersionsRepository extends JpaRepository<UserVersions, Long> {

    @Query("SELECT uv FROM UserVersions uv WHERE uv.user.id = :userId ORDER BY uv.dateOfCreation DESC")
    List<UserVersions> findLatestVersionByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT * FROM user_version where phone = :phone ORDER BY creation_time DESC", nativeQuery = true)
    List<UserVersions> findLatestVersionByUsername(@Param("phone") String phone);

    boolean existsByPhone(String phone);
}