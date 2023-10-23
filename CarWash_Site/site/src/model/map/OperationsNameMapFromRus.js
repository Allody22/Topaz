const operationsNameMapFromRus = {
    "Регистрация клиента": "User_sign_up",
    "Логин клиента": "User_sign_in",
    "Выход клиента": "User_sign_out",
    "Вход админа с проверкой роли": "Admin_log_in",
    "Проверки роли админа": "Admin_role_check",
    "Получение кода подтверждения для номера": "User_get_phone_code",
    "Поиск информации о клиента по телефону": "Find_user_by_telephone",
    "Получение заказов клиента": "Get_user_orders_by_admin",
    "Создание новой услуги": "Create_new_service_by_admin",

    "Обновление информации об услуге мойки": "Update_washing_service_by_admin",
    "Обновление информации об услуге полировки": "Update_polishing_service_by_admin",
    "Обновление информации об услуге шиномонтажа": "Update_tire_service_by_admin",

    "Получение всех пользователей админом": "Get_all_user_name_by_admin",
    "Бронирование заказа мойки": "Book_washing_order",
    "Бронирование заказа полировки": "Book_polishing_order",
    "Бронирование заказа шиномонтажа": "Book_tire_order",

    "Обновление информации о пользователе админом": "Update_user_info_by_admin",
    "Создание заказа мойки": "Create_washing_order",
    "Создание заказа полировки": "Create_polishing_order",
    "Создание заказа шиномонтажа": "Create_tire_order",
    "Удаление заказа": "Delete_order",
    "Обновление информации о заказе": "Update_order_info",

    "Получение информации об услуге": "Get_service_info",
    "Получение информации о заказе": "Get_order_info",
    "Получение всех услуг мойки": "Get_all_washing_services",
    "Получение всех актуальных услуг мойки": "Get_actual_washing_services",
    "Получение всех услуг мойки с ценой и временем выполнения": "Get_all_washing_services_price_time",
    "Получение актуальных услуг полировки": "Get_actual_polishing_orders",

    "Получение услуг полировки с ценой и временем": "Get_all_polishing_services_price_time",
    "Получение услуг шиномонтажа": "Get_actual_tire_orders",
    "Получение услуг шиномонтажа с ценой и временем": "Get_all_tire_services_price_time",
    "Получение заказов за один день": "Get_booked_time_in_one_day",
    "Получение не сделанных заказов": "Get_not_made_orders",
    "Получение цены и времени заказов для сайта": "Get_price_time_of_orders_site",

    "Получение цены и времени заказов": "Get_price_time",
    "Обновление профиля пользователя": "Update_user_info_by_user",
    "Получение своих заказов": "Get_user_orders_by_user",
}

export default operationsNameMapFromRus;
