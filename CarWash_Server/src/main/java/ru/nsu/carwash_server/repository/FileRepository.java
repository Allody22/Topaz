package ru.nsu.carwash_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.nsu.carwash_server.models.File;

import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {

    @Query(value = "SELECT * FROM files WHERE name = :Name AND version = " +
            "(SELECT MAX(version) FROM files WHERE name = :Name)", nativeQuery = true)
    Optional<File> findLatestVersionByName(@Param("Name") String name);

    @Query(value = "SELECT * FROM files WHERE name LIKE CONCAT('%', :name, '%') AND version = " +
            "(SELECT MAX(version) FROM files WHERE name LIKE CONCAT('%', :name, '%'))", nativeQuery = true)
    Optional<File> checkByName(@Param("name") String name);

    @Query(value = "SELECT * FROM files WHERE name LIKE CONCAT('%', :name, '%') " +
            "AND status LIKE CONCAT('%', :status, '%') " +
            "AND version = (SELECT MAX(version) FROM files WHERE name LIKE CONCAT('%', :name, '%') AND status LIKE CONCAT('%', :status, '%'))",
            nativeQuery = true)
    Optional<File> checkByNameAndStatus(@Param("name") String name, @Param("status") String status);


    @Query(value = "DELETE FROM files WHERE name LIKE CONCAT('%', :name, '%') AND version = :version", nativeQuery = true)
    Optional<File> deleteByNameAndVersion(@Param("name") String name, @Param("version") int version);


    @Query(value = "DELETE FROM files WHERE name LIKE CONCAT('%', :name, '%')", nativeQuery = true)
    void deleteAllByName(@Param("name") String name);

    @Query(value = "SELECT * FROM files WHERE url = :URL AND version = " +
            "(SELECT MAX(version) FROM files WHERE url = :URL)", nativeQuery = true)
    Optional<File> findLatestVersionByURL(@Param("URL") String url);

    @Query(value = "SELECT * FROM files WHERE url LIKE CONCAT('%', :url, '%') AND version = " +
            "(SELECT MAX(version) FROM files WHERE url LIKE CONCAT('%', :url, '%'))", nativeQuery = true)
    Optional<File> checkByURL(@Param("url") String url);

    @Query(value = "SELECT * FROM files WHERE url LIKE CONCAT('%', :url, '%') " +
            "AND status LIKE CONCAT('%', :status, '%') AND version = (SELECT MAX(version)" +
            " FROM files WHERE url LIKE CONCAT('%', :url, '%') AND status LIKE CONCAT('%', :status, '%'))",
            nativeQuery = true)
    Optional<File> checkByURLAndStatus(@Param("url") String url, @Param("status") String status);

    @Query(value = "SELECT * FROM files WHERE status LIKE CONCAT('%', :status, '%') " +
            "AND version = (SELECT MAX(version) FROM files WHERE status LIKE CONCAT('%', :status, '%'))",
            nativeQuery = true)
    Optional<File> findLatestVersionByStatus(@Param("status") String status);


    @Query(value = "SELECT * FROM files WHERE name = :Name AND version = :Version", nativeQuery = true)
    Optional<File> findAllByVersionAndName(@Param("Name") String name, @Param("Version") int version);

}
