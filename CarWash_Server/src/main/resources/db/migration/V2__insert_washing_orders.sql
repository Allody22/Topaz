INSERT INTO orders_washing(name, creation_time, price_first_type, price_second_type,
                           price_third_type, time_first_type, time_second_type,
                           time_third_type, associated_order, role)
VALUES ('Турбо_сушка_кузова', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 550, 600, 660, 25, 25, 25, 'NO',
        'additional'),
       ('Продувка_кузова', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 110, 140, 160, 7, 7, 7, 'NO',
        'additional'),
       ('Продувка_замков,зеркала', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 70, 70, 70, 5, 5, 5, 'NO',
        'additional'),
       ('Обработка_силиконом', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 110, 140, 160, 5, 5, 5, 'NO',
        'additional'),
       ('Обработка_замков_жидкостью', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 70, 70, 70, 2, 2, 2, 'NO',
        'additional'),
       ('Обработка_кожи_кондиционером_1_эл.', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 110, 110, 110, 7, 7, 7,
        'VIP;ELITE;Стандарт;Эконом;Комфорт',
        'additional'),
       ('Полироль_пластика_салона', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 140, 150, 180, 5, 5, 5,
        'VIP;ELITE;Стандарт;Эконом;Комфорт', 'additional'),
       ('Полироль_пластика_панель', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 80, 100, 100, 3, 3, 3,
        'VIP;ELITE;Стандарт;Эконом;Комфорт', 'additional'),
       ('Полироль_пластика_багажник', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 60, 80, 90, 3, 3, 3, 'NO',
        'additional'),
       ('Наружная_мойка_радиатора', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 90, 110, 170, 0, 0, 0, 'NO',
        'additional'),
       ('Чернение_шин_4_шт', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 110, 170, 170, 3, 3, 3,
        'VIP;ELITE;Стандарт;Эконом;Комфорт', 'additional'),
       ('Озонирование_салона_30_мин.', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1100, 1100, 1100, 30, 30, 30,
        'VIP;ELITE;Стандарт;Эконом', 'additional'),
       ('Очистка_битумных_пятен_кузов', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1100, 1650, 1650, 30, 30, 30,
        'VIP;ELITE;Стандарт;Комфорт',
        'additional'),
       ('Покрытие_лобового_стекла_Nano_Glass_1_эл.', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 550, 660, 770,
        15, 15, 15,
        'VIP;ELITE;Стандарт;Эконом;Комфорт', 'additional'),
       ('Покрытие_бокового_стекла_Nano_Glass_1_эл.', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 220, 280, 330,
        15, 15, 15,
        'VIP;ELITE;Стандарт;Эконом;Комфорт', 'additional'),
       ('Комплекс_всех_стёкол', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1650, 2200, 2750, 30, 30, 30, 'NO',
        'additional'),
       ('Диэлектрическая_химчистка_двигателя', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 900, 1000, 1100, 40,
        40, 40, 'NO', 'additional'),
       ('Химчистка_дисков_4_шт.', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 660, 900, 1100, 20, 20, 20,
        'VIP;ELITE;Стандарт;Эконом;Комфорт', 'additional'),
       ('Химчистка_багажника', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1100, 1650, 1650, 120, 180, 300, 'NO',
        'additional'),
       ('Химчистка_двери_1_эл.', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 550, 550, 550, 20, 30, 40, 'NO',
        'additional'),
       ('Химчистка_кресло_(текстиль)_1_эл.', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 550, 550, 550, 20, 30,
        40, 'NO', 'additional'),
       ('Химчистка_кресло_(кожа)_1_эл.', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 550, 550, 550, 15, 25, 30,
        'NO', 'additional'),
       ('Химчистка_передней_панели', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1100, 1100, 1650, 20, 35, 60,
        'NO', 'additional'),
       ('Химчистка_пола', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1100, 1650, 1650, 3, 5, 8, 'NO',
        'additional'),
       ('Химчистка_потолка', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1000, 1650, 1650, 55, 80, 120, 'NO',
        'additional'),
       ('Однофазная_мойка_с_химией_без_протирки', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 280, 330, 350, 20,
        20, 20, 'ЭкономSolo', 'main'),
       ('Мойка_кузова_2_фазы_с_протиркой', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 470, 550, 600, 40, 40, 40,
        'КомфортSolo;VIPSolo', 'main'),
       ('Мойка_кузова_2_фазы_без_протирки', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 380, 460, 520, 30, 30,
        30, 'СтандартSolo', 'main'),
       ('Мойка_комплекс_(кузов_1_фаза_+_салон)', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 680, 720, 820, 40,
        50, 60, 'NO', 'main'),
       ('Мойка_комплекс_(кузов_2_фазы_+_салон)', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 900, 1000, 1200, 60,
        70, 80, 'ELITESolo', 'main'),
       ('Покрытие_кварцевой_защитой', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 660, 720, 770, 5, 5, 5,
        'ELITE;Стандарт;Комфорт;VIPSolo', 'main'),
       ('Мойка_двигателя_с_хим._раствором_+_сушка', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 440, 500, 500,
        20, 20, 20, 'NO', 'main'),
       ('Очистка_арок_колес', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 130, 180, 220, 15, 15, 15, 'NO',
        'main'),
       ('Уборка_багажника', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 170, 220, 280, 20, 20, 20, 'NO', 'main'),
       ('Влажная_уборка_салона', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 160, 210, 270, 20, 20, 20, 'NO',
        'main'),
       ('Влажная_уборка_передней_панели', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 90, 110, 100, 10, 10, 10,
        'NO', 'main'),
       ('Пылесос_салона', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 160, 210, 270, 20, 20, 20, 'NO', 'main'),
       ('Пылесос_пола', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 100, 110, 150, 15, 15, 15, 'NO', 'main'),
       ('Пылесос_ковриков', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 60, 70, 90, 10, 10, 10, 'NO', 'main'),
       ('Коврик_багажников', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 70, 70, 70, 5, 5, 5, 'NO', 'main'),
       ('Резиновый_коврик_1_шт.', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 20, 20, 20, 3, 3, 3, 'NO', 'main'),
       ('Стирка_текстильного_коврика_1_шт.', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 40, 40, 40, 5, 5, 5,
        'NO', 'main'),
       ('Чистка_стёкол_с_2х_сторон', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 170, 220, 270, 20, 20, 20, 'NO',
        'main'),
       ('Чистка_стёкол_внутри_салона', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 160, 210, 270, 15, 15, 15,
        'NO', 'main'),
       ('Чистка_ветрового_стекла', CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 80, 100, 150, 5, 5, 5, 'NO',
        'main');