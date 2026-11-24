CREATE TABLE room (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(100) NOT NULL UNIQUE,
    capacity INT          NOT NULL
);

CREATE TABLE app_user (
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE booking (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id    BIGINT                      NOT NULL REFERENCES room (id),
    user_id    BIGINT                      NOT NULL REFERENCES app_user (id),
    start_time TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    end_time   TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_booking_room_time ON booking (room_id, start_time);

INSERT INTO room (name, capacity) VALUES ('Atlas', 8), ('Borealis', 4), ('Cosmos', 12);

INSERT INTO app_user (name, email) VALUES
    ('Alice', 'alice@example.com'),
    ('Bob', 'bob@example.com'),
    ('Carol', 'carol@example.com');
