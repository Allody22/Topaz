CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,
    creation_time TIMESTAMP WITH TIME ZONE
);

CREATE TABLE user_version
(
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT       NOT NULL,
    creation_time TIMESTAMP,
    phone         VARCHAR(255),
    email         VARCHAR(255),
    bonuses       INT,
    password      VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255),
    admin_note    TEXT,
    comments      TEXT,
    version       INT,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE roles
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE user_roles
(
    user_id BIGINT,
    role_id INT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE refresh_token
(
    id          SERIAL PRIMARY KEY,
    user_id     BIGINT,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP    NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE orders
(
    id            SERIAL PRIMARY KEY,
    creation_time TIMESTAMP,
    user_id       BIGINT,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE orders_washing
(
    id                SERIAL PRIMARY KEY,
    creation_time     TIMESTAMP,
    name              VARCHAR(255) NOT NULL UNIQUE,
    price_first_type  INT,
    price_second_type INT,
    price_third_type  INT,
    time_first_type   INT,
    time_second_type  INT,
    time_third_type   INT,
    role              VARCHAR(255),
    associated_order  VARCHAR(255)
);

CREATE TABLE orders_polishing
(
    id                SERIAL PRIMARY KEY,
    creation_time     TIMESTAMP,
    name              VARCHAR(255) NOT NULL UNIQUE,
    price_first_type  INT,
    price_second_type INT,
    price_third_type  INT,
    time_first_type   INT,
    time_second_type  INT,
    time_third_type   INT
);

CREATE TABLE orders_tire
(
    id            SERIAL PRIMARY KEY,
    creation_time TIMESTAMP,
    name          VARCHAR(255) NOT NULL UNIQUE,
    price_r_13    INT,
    price_r_14    INT,
    price_r_15    INT,
    price_r_16    INT,
    price_r_17    INT,
    price_r_18    INT,
    price_r_19    INT,
    price_r_20    INT,
    price_r_21    INT,
    price_r_22    INT,
    time_r_13     INT,
    time_r_14     INT,
    time_r_15     INT,
    time_r_16     INT,
    time_r_17     INT,
    time_r_18     INT,
    time_r_19     INT,
    time_r_20     INT,
    time_r_21     INT,
    time_r_22     INT,
    role          VARCHAR(255)
);



CREATE TABLE orders_versions
(
    id             SERIAL PRIMARY KEY,
    order_id       BIGINT NOT NULL,
    creation_time  TIMESTAMP,
    start_time     TIMESTAMP,
    end_time       TIMESTAMP,
    administrator  VARCHAR(255),
    specialist     VARCHAR(255),
    auto_number    VARCHAR(255),
    sale           VARCHAR(255),

    auto_type      INT,
    box_number     INT,
    bonuses        INT,
    price          INT,
    wheel_radius   VARCHAR(255),
    comments       TEXT,
    user_contacts  VARCHAR(255),
    order_type     VARCHAR(255),
    current_status VARCHAR(255),
    version        INT,
    FOREIGN KEY (order_id) REFERENCES orders (id)
);

CREATE TABLE orders_washing_link
(
    id         SERIAL PRIMARY KEY,
    order_id   BIGINT,
    washing_id BIGINT,
    FOREIGN KEY (order_id) REFERENCES orders_versions (id),
    FOREIGN KEY (washing_id) REFERENCES orders_washing (id)
);

-- Таблица связи для orders_polishing
CREATE TABLE orders_polishing_link
(
    id           SERIAL PRIMARY KEY,
    order_id     BIGINT,
    polishing_id BIGINT,
    FOREIGN KEY (order_id) REFERENCES orders_versions (id),
    FOREIGN KEY (polishing_id) REFERENCES orders_polishing (id)
);

-- Таблица связи для orders_tire
CREATE TABLE orders_tire_link
(
    id       SERIAL PRIMARY KEY,
    order_id BIGINT,
    tire_id  BIGINT,
    FOREIGN KEY (order_id) REFERENCES orders_versions (id),
    FOREIGN KEY (tire_id) REFERENCES orders_tire (id)
);

CREATE TABLE files
(
    id            SERIAL PRIMARY KEY,
    creation_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    name          VARCHAR(255),
    url           VARCHAR(255),
    status        VARCHAR(255),
    description   VARCHAR(255),
    version       INTEGER
);

CREATE TABLE operations
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(255),
    description VARCHAR(255)
);

CREATE TABLE operations_versions
(
    id            SERIAL PRIMARY KEY,
    operations_id INTEGER REFERENCES operations (id) ON DELETE CASCADE,
    creation_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    changes       VARCHAR(255),
    version       INTEGER
);


CREATE TABLE operations_users_link
(
    id                     SERIAL PRIMARY KEY,
    creation_time          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    operations_versions_id INTEGER REFERENCES operations_versions (id) ON DELETE CASCADE,
    user_id                INTEGER REFERENCES users (id) ON DELETE CASCADE,
    description            TEXT
);

