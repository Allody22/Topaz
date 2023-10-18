package ru.nsu.carwash_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.nsu.carwash_server.models.FileEntity;

import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

    @Query(value = "SELECT * FROM files WHERE name = :Name AND version = " +
            "(SELECT MAX(version) FROM files WHERE name = :Name)", nativeQuery = true)
    Optional<FileEntity> findLatestVersionByName(@Param("Name") String name);

    @Query(value = "SELECT * FROM files WHERE name LIKE CONCAT('%', :name, '%') AND version = " +
            "(SELECT MAX(version) FROM files WHERE name LIKE CONCAT('%', :name, '%'))", nativeQuery = true)
    Optional<FileEntity> checkByName(@Param("name") String name);

    @Query(value = "SELECT * FROM files WHERE name LIKE CONCAT('%', :name, '%') " +
            "AND status LIKE CONCAT('%', :status, '%') " +
            "AND version = (SELECT MAX(version) FROM files WHERE name LIKE CONCAT('%', :name, '%') AND status LIKE CONCAT('%', :status, '%'))",
            nativeQuery = true)
    Optional<FileEntity> checkByNameAndStatus(@Param("name") String name, @Param("status") String status);


    @Query(value = "DELETE FROM files WHERE name LIKE CONCAT('%', :name, '%') AND version = :version", nativeQuery = true)
    Optional<FileEntity> deleteByNameAndVersion(@Param("name") String name, @Param("version") int version);


    @Query(value = "DELETE FROM files WHERE name LIKE CONCAT('%', :name, '%')", nativeQuery = true)
    void deleteAllByName(@Param("name") String name);

    @Query(value = "SELECT * FROM files WHERE url = :URL AND version = " +
            "(SELECT MAX(version) FROM files WHERE url = :URL)", nativeQuery = true)
    Optional<FileEntity> findLatestVersionByURL(@Param("URL") String url);

    @Query(value = "SELECT * FROM files WHERE url LIKE CONCAT('%', :url, '%') AND version = " +
            "(SELECT MAX(version) FROM files WHERE url LIKE CONCAT('%', :url, '%'))", nativeQuery = true)
    Optional<FileEntity> checkByURL(@Param("url") String url);

    @Query(value = "SELECT * FROM files WHERE url LIKE CONCAT('%', :url, '%') " +
            "AND status LIKE CONCAT('%', :status, '%') AND version = (SELECT MAX(version)" +
            " FROM files WHERE url LIKE CONCAT('%', :url, '%') AND status LIKE CONCAT('%', :status, '%'))",
            nativeQuery = true)
    Optional<FileEntity> checkByURLAndStatus(@Param("url") String url, @Param("status") String status);

    @Query(value = "SELECT * FROM files WHERE status LIKE CONCAT('%', :status, '%') " +
            "AND version = (SELECT MAX(version) FROM files WHERE status LIKE CONCAT('%', :status, '%'))",
            nativeQuery = true)
    Optional<FileEntity> findLatestVersionByStatus(@Param("status") String status);


    @Query(value = "SELECT * FROM files WHERE name = :Name AND version = :Version", nativeQuery = true)
    Optional<FileEntity> findAllByVersionAndName(@Param("Name") String name, @Param("Version") int version);

}
