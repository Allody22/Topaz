package ru.nsu.carwash_server.repository.operations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.nsu.carwash_server.models.operations.Operations;

import java.util.List;

@Repository
public interface OperationsRepository extends JpaRepository<Operations, Long> {

    @Query("SELECT o.name FROM Operations o")
    List<String> findAllOperationNames();
}
