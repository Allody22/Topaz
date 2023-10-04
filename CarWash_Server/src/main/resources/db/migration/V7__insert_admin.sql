INSERT INTO users (creation_time)
VALUES (CURRENT_TIMESTAMP);

DO
$$
    DECLARE
        last_user_id BIGINT;
    BEGIN
        SELECT id INTO last_user_id FROM users ORDER BY id DESC LIMIT 1;

        INSERT INTO user_version (user_id, creation_time, phone, email, password, bonuses, full_name, admin_note,
                                  comments, version)
        VALUES (last_user_id, CURRENT_TIMESTAMP, '70000000000', 'm.bogdanov2@g.nsu.ru',
                '$2a$10$ZfXJNhfA4vnK2Z6Fg9j8yOeQ2zeXJCGfOr28dG9hDUoeCnNvNGUA.', 0,
                'Богдан Лапушинский Селезнёвич', 'Создание изначального админа', 'Комментарии самого чела', 1);

        INSERT INTO user_roles (user_id, role_id)
        SELECT last_user_id, id
        FROM roles;

    END
$$;
