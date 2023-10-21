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
    phone         VARCHAR(20),
    email         VARCHAR(40),
    bonuses       INT,
    password      VARCHAR(255) NOT NULL,
    full_name     VARCHAR(80),
    admin_note    VARCHAR(120),
    comments      VARCHAR(120),
    version       INT CHECK (user_version.version >= 0),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE roles
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL
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
    name              VARCHAR(120) NOT NULL UNIQUE,
    price_first_type  INT CHECK (orders_washing.price_first_type >= 0),
    price_second_type INT CHECK (orders_washing.price_second_type >= 0),
    price_third_type  INT CHECK (orders_washing.price_third_type >= 0),
    time_first_type   INT CHECK (orders_washing.time_first_type >= 0),
    time_second_type  INT CHECK (orders_washing.time_second_type >= 0),
    time_third_type   INT CHECK (orders_washing.time_third_type >= 0),
    role              VARCHAR(30),
    associated_order  VARCHAR(200)
);

CREATE TABLE orders_polishing
(
    id                SERIAL PRIMARY KEY,
    creation_time     TIMESTAMP,
    name              VARCHAR(120) NOT NULL UNIQUE,
    price_first_type  INT CHECK (orders_polishing.price_first_type >= 0),
    price_second_type INT CHECK (orders_polishing.price_second_type >= 0),
    price_third_type  INT CHECK (orders_polishing.price_third_type >= 0),
    time_first_type   INT CHECK (orders_polishing.time_first_type >= 0),
    time_second_type  INT CHECK (orders_polishing.time_second_type >= 0),
    time_third_type   INT CHECK (orders_polishing.time_third_type >= 0)
);

CREATE TABLE orders_tire
(
    id            SERIAL PRIMARY KEY,
    creation_time TIMESTAMP,
    name          VARCHAR(120) NOT NULL UNIQUE,
    price_r_13    INT CHECK (orders_tire.price_r_13 >= 0),
    price_r_14    INT CHECK (orders_tire.price_r_14 >= 0),
    price_r_15    INT CHECK (orders_tire.price_r_15 >= 0),
    price_r_16    INT CHECK (orders_tire.price_r_16 >= 0),
    price_r_17    INT CHECK (orders_tire.price_r_17 >= 0),
    price_r_18    INT CHECK (orders_tire.price_r_18 >= 0),
    price_r_19    INT CHECK (orders_tire.price_r_19 >= 0),
    price_r_20    INT CHECK (orders_tire.price_r_20 >= 0),
    price_r_21    INT CHECK (orders_tire.price_r_21 >= 0),
    price_r_22    INT CHECK (orders_tire.price_r_22 >= 0),
    time_r_13     INT CHECK (orders_tire.time_r_13 >= 0),
    time_r_14     INT CHECK (orders_tire.time_r_14 >= 0),
    time_r_15     INT CHECK (orders_tire.time_r_15 >= 0),
    time_r_16     INT CHECK (orders_tire.time_r_16 >= 0),
    time_r_17     INT CHECK (orders_tire.time_r_17 >= 0),
    time_r_18     INT CHECK (orders_tire.time_r_18 >= 0),
    time_r_19     INT CHECK (orders_tire.time_r_19 >= 0),
    time_r_20     INT CHECK (orders_tire.time_r_20 >= 0),
    time_r_21     INT CHECK (orders_tire.time_r_21 >= 0),
    time_r_22     INT CHECK (orders_tire.time_r_22 >= 0),
    role          VARCHAR(30)
);



CREATE TABLE orders_versions
(
    id             SERIAL PRIMARY KEY,
    order_id       BIGINT NOT NULL,
    creation_time  TIMESTAMP,
    start_time     TIMESTAMP,
    end_time       TIMESTAMP,
    administrator  VARCHAR(50),
    specialist     VARCHAR(50),
    auto_number    VARCHAR(50),
    sale           VARCHAR(255),
    auto_type      INT,
    box_number     INT CHECK (orders_versions.box_number >= 0),
    bonuses        INT,
    price          INT CHECK (orders_versions.price >= 0),
    wheel_radius   VARCHAR(20),
    comments       VARCHAR(255),
    user_contacts  VARCHAR(50),
    order_type     VARCHAR(30),
    current_status VARCHAR(100),
    version        INT CHECK (orders_versions.version >= 0),
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
    version       INT CHECK (files.version >= 0)
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
    version       INT CHECK (operations_versions.version >= 0)
);


CREATE TABLE operations_users_link
(
    id                     SERIAL PRIMARY KEY,
    creation_time          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    operations_versions_id INTEGER REFERENCES operations_versions (id) ON DELETE CASCADE,
    user_id                INTEGER REFERENCES users (id) ON DELETE CASCADE,
    description            TEXT
);

