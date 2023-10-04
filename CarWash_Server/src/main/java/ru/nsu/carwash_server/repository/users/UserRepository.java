package ru.nsu.carwash_server.repository.users;


import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.nsu.carwash_server.models.users.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Override
    @NonNull
    Optional<User> findById(@NonNull Long aLong);


    @Query(value = "SELECT * FROM users", nativeQuery = true)
    List<User> getAllUsers();
}