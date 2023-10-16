package ru.nsu.carwash_server.services.interfaces;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import ru.nsu.carwash_server.models.File;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface FileService {


    Resource loadWithCache(String filename);

    void clearCache();

    /**
     * Возврат всех файлов в одном запросе
     *
     * @return список Resource
     */
    List<Resource> loadAllFiles();

    /**
     * Находим в репозиторие последнюю версию
     *
     * @param name - имя картинке
     * @return файл, если такой есть
     */
    Optional<File> findByLatestByNameInRepo(String name);

    /**
     * Проверяем, что файл с таким именем есть
     *
     * @param name - имя файла
     * @return файл, если такой есть
     */
    Optional<File> checkByName(String name);

    /**
     * Проверяем, что файл с таким именем и статусом есть
     *
     * @param name   - имя файла
     * @param status - статус файла
     * @return файл, если такой есть
     */
    Optional<File> checkByNameAndStatus(String name, String status);

    /**
     * Находит последний файл с таким url
     * в репозитории
     *
     * @param url - необходимый url
     * @return файл, если такой есть
     */
    Optional<File> findLatestByUrlInRepo(String url);

    /**
     * Проверка того, что файл с таким url должен был
     *
     * @param url - необходимый url
     * @return файл, если такой есть
     */
    Optional<File> checkByUrl(String url);


    /**
     * Проверка, что файл с таким url и статусом есть
     *
     * @param url    - необходимый url
     * @param status - необходимый статус
     * @return файл, если такой есть
     */
    Optional<File> checkByUrlAndStatus(String url, String status);

    /**
     * Поиск последней версии файл с конкретным статусом
     *
     * @param status - необходимый статус
     * @return файл, если такой есть
     */
    Optional<File> findLatestVersionByStatus(String status);

    /**
     * Сохранение новой версии файла
     * с конкретным статусом
     *
     * @param file        - сохраняемый файл
     * @param description -описание файла
     * @param status      - статус файла
     */
    void saveByStatus(MultipartFile file, String description, String status);

    /**
     * Сохранение новой версии файла по url
     *
     * @param file        - сохраняемый файл
     * @param description -описание файла
     * @param status      - статус файла
     */
    void saveByURL(MultipartFile file, String description, String status);

    /**
     * Получение всех файл из папки
     *
     * @return все файлы из папки
     */
    Stream<Path> loadAll();

    /**
     * Полное удаление файла, а не
     * создание новой удалённой версии
     *
     * @param filename - имя файла
     * @return true - если операция успешна.
     */
    boolean delete(String filename);

    /**
     * Получение отдельного файла
     *
     * @param filename - имя файла
     * @return возвращаемый файл
     */
    Resource load(String filename);

}