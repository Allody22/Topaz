INSERT INTO orders_polishing(name, creation_time, price_first_type, price_second_type,
                             price_third_type, time_first_type, time_second_type,
                             time_third_type)
VALUES ('Полировка_восстановительная', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 8000, 10000, 14000, 480, 480,
        480),
       ('Глубокая_абразивная_полировка', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 12000, 14000, 18000, 1680,
        1680, 1680),
       ('Полировка_фар_1_шт.', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 500, 500, 500, 20, 20, 20),
       ('Полимер_Sonax_до_6_месяцев', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1500, 1500, 1500, 300, 300,
        300),
       ('Кварцекерамическое_покрытие_CAN_COAT_до_6_месяцев', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 5000,
        5000, 500, 480, 480, 480),
       ('Koch_Chemie_1K-NANO_1_год', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 7000, 8000, 10000, 480, 480,
        480),
       ('Профессиональное_покрытие_керамика_(2_слоя_+_1_слой)_до_3_лет',
        CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk',
        20000, 22000, 25000, 1440, 1440, 1440);