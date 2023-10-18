package ru.nsu.carwash_server.services.interfaces;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import ru.nsu.carwash_server.models.FileEntity;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface FileService {


    /**
     * Сохранение обновлённоё картинки
     *
     * @param file            - файл для сохранения
     * @param description     - описание файла
     * @param status          - статус файл
     * @param previousVersion - прошлая версия файла, если такая имеется
     * @param newFileName     - новое имя файла
     * @return - кэш как сущность файла
     */
    FileEntity overwriteByStatus(MultipartFile file, String description, String status, FileEntity previousVersion, String newFileName);

    /**
     * Сохраняем картинку в первый раз
     *
     * @param file        - файл для сохранения
     * @param description - описание файла
     * @param status      - статус файла
     * @param newFileName - новое имя файла
     * @return - кэш как моя сущность файла
     */
    FileEntity saveByStatus(MultipartFile file, String description, String status, String newFileName);

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
    Optional<FileEntity> findByLatestByNameInRepo(String name);

    /**
     * Проверяем, что файл с таким именем есть
     *
     * @param name - имя файла
     * @return файл, если такой есть
     */
    Optional<FileEntity> checkByName(String name);

    /**
     * Проверяем, что файл с таким именем и статусом есть
     *
     * @param name   - имя файла
     * @param status - статус файла
     * @return файл, если такой есть
     */
    Optional<FileEntity> checkByNameAndStatus(String name, String status);

    /**
     * Находит последний файл с таким url
     * в репозитории
     *
     * @param url - необходимый url
     * @return файл, если такой есть
     */
    Optional<FileEntity> findLatestByUrlInRepo(String url);

    /**
     * Проверка того, что файл с таким url должен был
     *
     * @param url - необходимый url
     * @return файл, если такой есть
     */
    Optional<FileEntity> checkByUrl(String url);


    /**
     * Проверка, что файл с таким url и статусом есть
     *
     * @param url    - необходимый url
     * @param status - необходимый статус
     * @return файл, если такой есть
     */
    Optional<FileEntity> checkByUrlAndStatus(String url, String status);

    /**
     * Поиск последней версии файл с конкретным статусом
     *
     * @param status - необходимый статус
     * @return файл, если такой есть
     */
    Optional<FileEntity> findLatestVersionByStatus(String status);

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

    /**
     * Удаляем из кэша файл по его имена.
     *
     * @param filename - имя файла
     */
    void deleteAndEvict(String filename);


    /**
     * Удаляем весь имеющийся кеш
     */
    void evictAllCacheValues();

}