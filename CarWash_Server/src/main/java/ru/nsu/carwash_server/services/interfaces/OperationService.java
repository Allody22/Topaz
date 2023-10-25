package ru.nsu.carwash_server.services.interfaces;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import ru.nsu.carwash_server.models.operations.OperationsUserLink;
import ru.nsu.carwash_server.models.operations.OperationsVersions;
import ru.nsu.carwash_server.models.users.User;
import ru.nsu.carwash_server.payload.response.UserOperationsResponse;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface OperationService {

    /**
     * Поиск всех операций по названию операции
     *
     * @param operationName - название операции
     * @return список найденных операций
     */
    List<OperationsUserLink> getAllOperationsByName(String operationName);

    /**
     * Просто достаём все операции из таблицы
     *
     * @return список из всех операций
     */
    List<OperationsUserLink> getAllOperations();

    /**
     * Мы ищем самую последние единственное действие пользователя в таблице связей пользователей
     * с существующими действиями. Причём ищём по номеру телефона в описании операции,
     * а не в связи с пользователями, чтобы найти тех, кто пытался сделать действия,
     * будучи не зарегистрированным. Потом берём последнее действие, сделанное
     * в интервал времени, какое-то кол-во часов назад
     *
     * @param phoneNumber - номер телефона человека
     * @param advice      - слово-помощник по которому ищем действие
     * @param hourNumber  - кол-во часов
     * @return действие пользователя под условия, если такое существует
     */
    Optional<OperationsUserLink> findLatestByPhoneInLastHours(String phoneNumber, String advice, int hourNumber);

    /**
     * Мы просто парсим текст, чтобы достать из него код,
     * отправленный пользователю на смс.
     *
     * @param description - текст
     * @return код из данного текста
     */
    int extractCodeFromDescription(String description);

    /**
     * Ищем все действия пользователя по номеру телефона и части предложения
     * в описании операции за какой-то промежуток времени
     *
     * @param phoneNumber        - номер телефона пользователя
     * @param descriptionMessage - фраза присутствующая в предложении (описании операции)
     * @param time               - время на которого надо искать интервалы. Например: если при текущим 19 часах
     *                           передать в эту переменную 6, то будет от 13 часов текущего дня до текущего момента
     * @return список найденных операций
     */
    List<OperationsUserLink> getAllDescriptionOperationsByTime(String phoneNumber, String descriptionMessage, LocalDateTime time);


    /**
     * Метод для создания смс просто для отправки кода
     *
     * @param number - номер, на который отправится код
     * @return сущность для отправки запроса и секретный код
     * @throws JsonProcessingException возможна ошибка при работе с джейсоном
     */
    Pair<HttpEntity<String>, Integer> createSmsCode(String number) throws JsonProcessingException;

    /**
     * Получаем последний код, полученный пользователем
     *
     * @param phoneNumber - номер телефона пользователя
     * @return сам секретный код
     */
    int getLatestCodeByPhoneNumber(String phoneNumber);

    /**
     * Получаем последний код, отправленные пользователю за
     * последние несколько минут
     *
     * @param phoneNumber   - телефон для проверки
     * @param context       - указываем что нас интересует операция с смс
     * @param minutesNumber - какой промежуток времени проверяем
     * @return действие пользователя с кодом, если такое есть
     */
    Optional<OperationsUserLink> findLatestByPhoneInMinutes(String phoneNumber, String context, int minutesNumber);

    /**
     * Сохраняем операцию, сделанную конкретным пользователем
     *
     * @param operationName      - название операции
     * @param user               - пользователь для отношения с операцией
     * @param descriptionMessage - описание операции по действиям
     * @param version            - версия операции
     */
    void SaveUserOperation(String operationName, User user, String descriptionMessage, int version);

    /**
     * Получение всех операций пользователя по айди этого пользователя или его номеру
     *
     * @param id    - айди пользователя
     * @param phone - номер телефона пользователя
     * @return список операций пользователя
     */
    List<OperationsUserLink> getAllUserOperationsByIdOrPhone(Long id, String phone);

    /**
     * Получение названий всех операций на английском
     *
     * @return список из названий всех операций
     */
    List<String> getAllOperationsNames();

    /**
     * Получение определённой версии операции по её названии
     *
     * @param name    - название операции
     * @param version - версия операции
     * @return сама операция
     */
    OperationsVersions getOperationVersionByNameAndVersion(String name, Integer version);

    /**
     * Метод для перевода операций в удобную для просмотра форму
     * для их возврата из запроса
     *
     * @param operationsVersions - набор операций
     * @return новая форма операции
     */
    List<UserOperationsResponse> getRationalOperationForm(List<OperationsVersions> operationsVersions);

    /**
     * Получаем все операции всех пользователей в каком-то
     * промежутке времени
     *
     * @param startTime - время поиска начала
     * @param endTime   - время поиска конца
     * @return список операций
     */
    List<OperationsUserLink> getAllOperationsInATime(Date startTime, Date endTime);

}
