package ru.nsu.carwash_server.repository.operations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.carwash_server.models.operations.Operations;

@Repository
public interface OperationsRepository extends JpaRepository<Operations, Long> {
}
