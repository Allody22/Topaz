package ru.nsu.carwash_server.repository.operations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.nsu.carwash_server.models.OperationsVersions;

import java.util.Optional;

@Repository
public interface OperationsVersionsRepository extends JpaRepository<OperationsVersions, Long> {

    @Query(value = "SELECT * FROM operations_versions WHERE operations_id = :operationId " +
            "ORDER BY creation_time DESC LIMIT 1", nativeQuery = true)
    Optional<OperationsVersions> findLatestVersionByOperations_Id(@Param("operationId") Long operationId);

    @Query(value = "SELECT * FROM operations_versions WHERE version = :version " +
            "AND operations_id = :operationId", nativeQuery = true)
    Optional<OperationsVersions> findOperationsVersionsByVersionAndOperations_Id(@Param("version") Integer version,
                                                                                 @Param("operationId") Long operationId);

    @Query("SELECT ov FROM OperationsVersions ov JOIN ov.operations op WHERE op.name = :operationName AND ov.version = :versionNumber")
    Optional<OperationsVersions> findByOperationNameAndVersion(@Param("operationName") String operationName, @Param("versionNumber") int versionNumber);

}