INSERT INTO operations (name, description)
VALUES ('User_sign_up', 'Регистрация клиента');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('User_sign_in', 'Логин клиента');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('User_sign_out', 'Выход клиента');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Admin_log_in', 'Вход админа с проверкой роли');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Admin_role_check', 'Проверки роли админа');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('User_get_phone_code', 'Проверка номера телефона');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Find_user_by_telephone', 'Поиск информации о клиента по телефону');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Get_user_orders_by_admin', 'Получение заказов клиента');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Create_new_service_by_admin', 'Создание новой услуги');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Update_washing_service_by_admin', 'Обновление информации об услуги мойки');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Update_polishing_service_by_admin', 'Обновление информации об услуги полировки');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Update_tire_service_by_admin', 'Обновление информации об услуги шиномонтажа');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Get_all_user_name_by_admin', 'Получение всех пользователей админом');
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


--Изменение и обновление информации
INSERT INTO operations (name, description)
VALUES ('Delete_order', 'Удаление заказа');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Update_order_info', 'Обновление информации о заказе');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

--Получение различной информации

INSERT INTO operations (name, description)
VALUES ('Get_service_info', 'Получение информации об услуге');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Get_order_info', 'Получение информации о заказе');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Get_all_washing_services', 'Получение всех услуг мойки');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Get_actual_washing_services', 'Получение всех актуальных услуг мойки');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Get_all_washing_services_price_time', 'Получение всех услуг мойки с ценой и временем выполнения');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Get_actual_polishing_orders', 'Получение актуальных услуг полировки');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Get_all_polishing_services_price_time', 'Получение услуг полировки с ценой и временем');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Get_actual_tire_orders', 'Получение услуг шиномонтажа');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Get_all_tire_services_price_time', 'Получение услуг шиномонтажа с ценой и временем');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Get_booked_time_in_one_day', 'Получение заказов за один день');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Get_not_made_orders', 'Получение не сделанных заказов');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Get_price_time_of_orders_site', 'Получение цены и времени заказов для сайта');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Get_price_time', 'Получение цены и времени заказов');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Update_user_info_by_user', 'Обновление профиля пользователя');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');

INSERT INTO operations (name, description)
VALUES ('Get_user_orders_by_user', 'Получение своих заказов');
INSERT INTO operations_versions (operations_id, creation_time, version, changes)
VALUES (currval('operations_id_seq'), CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Novosibirsk', 1, 'Initial version');
