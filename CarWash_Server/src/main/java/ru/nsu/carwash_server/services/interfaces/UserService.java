package ru.nsu.carwash_server.services.interfaces;

import ru.nsu.carwash_server.models.users.Role;
import ru.nsu.carwash_server.models.users.User;
import ru.nsu.carwash_server.models.users.UserVersions;

import java.util.List;
import java.util.Set;

public interface UserService {

    /**
     * Сохранение нового пользователя в БД
     *
     * @param user             - информация о сущности пользователя
     * @param roles            - роли пользователя
     * @param version          - версия пользователя
     * @param userFirstVersion - первая версия пользователя
     */
    void saveNewUser(User user, Set<Role> roles, int version, UserVersions userFirstVersion);

    /**
     * Проверки того, что пользователь с таким
     * именем существует
     *
     * @param username - имя для проверки
     * @return true - если пользователь существует, иначе false
     */
    boolean existByPhone(String username);

    /**
     * Получение всей информации и пользователе
     * со всеми версиями по айди
     *
     * @param userId - айди пользователя
     * @return сам пользователь
     */
    User getFullUserById(Long userId);

    /**
     * Получение всех актуальным username
     *
     * @return список строк с username
     */
    List<String> getAllActualPhones();

    /**
     * Получение последней версии пользователя
     * по его username
     *
     * @param username - имя для поиска
     * @return последняя версия пользователя
     */
    UserVersions getActualUserVersionByPhone(String username);

    /**
     * Получение последней версии пользователя
     * по его айди
     *
     * @param id - айди пользователя
     * @return пследняя версия
     */
    UserVersions getActualUserVersionById(Long id);
}
