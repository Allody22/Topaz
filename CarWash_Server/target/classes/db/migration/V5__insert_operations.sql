INSERT INTO operations (name, description)
VALUES ('User_sign_up', 'Регистрация клиента');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('User_get_phone_code', 'Получение кода подтверждения для номера');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('User_write_wrong_code', 'Пользователь вводит неверный код подтверждения для номера');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Create_new_service_by_admin', 'Создание новой услуги');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Update_washing_service_by_admin', 'Обновление информации об услуге мойки');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Update_polishing_service_by_admin', 'Обновление информации об услуге полировки');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Update_tire_service_by_admin', 'Обновление информации об услуге шиномонтажа');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Book_washing_order', 'Бронирование заказа мойки');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Book_polishing_order', 'Бронирование заказа полировки');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Book_tire_order', 'Бронирование заказа шиномонтажа');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

-- Создание заказов админом на сайте
INSERT INTO operations (name, description)
VALUES ('Update_user_info_by_admin', 'Обновление информации о пользователе админом');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Create_washing_order', 'Создание заказа мойки');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Create_polishing_order', 'Создание заказа полировки');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Create_tire_order', 'Создание заказа шиномонтажа');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Delete_order', 'Удаление заказа');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Update_order_info', 'Обновление информации о заказе');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Update_user_info_by_user', 'Обновление профиля пользователя');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('New_sale', 'Администратор добавил новую акцию');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');
