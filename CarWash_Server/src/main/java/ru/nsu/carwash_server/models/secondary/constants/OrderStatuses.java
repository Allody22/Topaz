package ru.nsu.carwash_server.models.secondary.constants;


import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class OrderStatuses {

    public final String cancelled = "cancelled";

    private final String technicalBoxCheck = "technicalBoxCheck";

    public final String createdPaid100PercentAndDone = "createdPaid100PercentAndDone";

    public final String createdPaid0PercentNotDone = "createdPaid0PercentNotDone";

    public final String createdPaid5PercentNotDone = "createdPaid5PercentNotDone";

    public final String createdPaid10PercentNotDone = "createdPaid10PercentNotDone";

    public final String createdPaid20PercentNotDone = "createdPaid20PercentNotDone";

    public final String createdPaid30PercentNotDone = "createdPaid30PercentNotDone";

    public final String createdPaid40PercentNotDone = "createdPaid40PercentNotDone";

    public final String createdPaid50PercentNotDone = "createdPaid50PercentNotDone";

    public final String createdPaid60PercentNotDone = "createdPaid60PercentNotDone";

    public final String createdPaid70PercentNotDone = "createdPaid70PercentNotDone";

    public final String createdPaid80PercentNotDone = "createdPaid80PercentNotDone";

    public final String createdPaid90PercentNotDone = "createdPaid90PercentNotDone";

    public final String createdPaid100PercentNotDone = "createdPaid100PercentNotDone";

    public final String createdPaid0PercentAndDone = "createdPaid0PercentAndDone";

    public final String createdPaid5PercentAndDone = "createdPaid5PercentAndDone";

    public final String createdPaid10PercentAndDone = "createdPaid10PercentAndDone";

    public final String createdPaid20PercentAndDone = "createdPaid20PercentAndDone";

    public final String createdPaid30PercentAndDone = "createdPaid30PercentAndDone";

    public final String createdPaid40PercentAndDone = "createdPaid40PercentAndDone";

    public final String createdPaid50PercentAndDone = "createdPaid50PercentAndDone";

    public final String createdPaid60PercentAndDone = "createdPaid60PercentAndDone";

    public final String createdPaid70PercentAndDone = "createdPaid70PercentAndDone";

    public final String createdPaid80PercentAndDone = "createdPaid80PercentAndDone";

    public final String createdPaid90PercentAndDone = "createdPaid90PercentAndDone";

    private static final Map<String, String> TRANSLATIONS;

    static {
        TRANSLATIONS = new HashMap<>();
        TRANSLATIONS.put(cancelled, "Отменён");
        TRANSLATIONS.put(technicalBoxCheck, "Техническая проверка бокса");
        TRANSLATIONS.put(createdPaid0PercentNotDone, "Не оплачен и не сделан");
        TRANSLATIONS.put(createdPaid5PercentNotDone, "Оплачен на 5 процентов и не сделан");
        TRANSLATIONS.put(createdPaid10PercentNotDone, "Оплачен на 10 процентов и не сделан");
        TRANSLATIONS.put(createdPaid20PercentNotDone, "Оплачен на 20 процентов и не сделан");
        TRANSLATIONS.put(createdPaid30PercentNotDone, "Оплачен на 30 процентов и не сделан");
        TRANSLATIONS.put(createdPaid40PercentNotDone, "Оплачен на 40 процентов и не сделан");
        TRANSLATIONS.put(createdPaid50PercentNotDone, "Оплачен на 50 процентов и не сделан");
        TRANSLATIONS.put(createdPaid60PercentNotDone, "Оплачен на 60 процентов и не сделан");
        TRANSLATIONS.put(createdPaid70PercentNotDone, "Оплачен на 70 процентов и не сделан");
        TRANSLATIONS.put(createdPaid80PercentNotDone, "Оплачен на 80 процентов и не сделан");
        TRANSLATIONS.put(createdPaid90PercentNotDone, "Оплачен на 90 процентов и не сделан");
        TRANSLATIONS.put(createdPaid100PercentNotDone, "Полностью оплачен и не сделан");
        TRANSLATIONS.put(createdPaid0PercentAndDone, "Не оплачен, но сделан");
        TRANSLATIONS.put(createdPaid5PercentAndDone, "Оплачен на 50 процентов и сделан");
        TRANSLATIONS.put(createdPaid10PercentAndDone, "Оплачен на 10 процентов и сделан");
        TRANSLATIONS.put(createdPaid20PercentAndDone, "Оплачен на 20 процентов и сделан");
        TRANSLATIONS.put(createdPaid30PercentAndDone, "Оплачен на 30 процентов и сделан");
        TRANSLATIONS.put(createdPaid40PercentAndDone, "Оплачен на 40 процентов и сделан");
        TRANSLATIONS.put(createdPaid50PercentAndDone, "Оплачен на 50 процентов и сделан");
        TRANSLATIONS.put(createdPaid60PercentAndDone, "Оплачен на 60 процентов и сделан");
        TRANSLATIONS.put(createdPaid70PercentAndDone, "Оплачен на 70 процентов и сделан");
        TRANSLATIONS.put(createdPaid80PercentAndDone, "Оплачен на 80 процентов и сделан");
        TRANSLATIONS.put(createdPaid90PercentAndDone, "Оплачен на 90 процентов и сделан");
        TRANSLATIONS.put(createdPaid100PercentAndDone, "Полностью оплачен и сделан");
    }

    public static String getTranslatedStatus(String status) {
        return TRANSLATIONS.getOrDefault(status, "Неизвестный статус");
    }
}