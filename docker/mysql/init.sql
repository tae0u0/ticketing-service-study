-- ────────────────────────── Databases ──────────────────────────
CREATE DATABASE IF NOT EXISTS ticketing_user CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS ticketing_event CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS ticketing_reservation CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS ticketing_payment CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS ticketing_booking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS ticketing_notification CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ────────────────────────── Users ──────────────────────────
CREATE USER IF NOT EXISTS 'ticketing'@'%' IDENTIFIED BY 'ticketing1234';
GRANT ALL PRIVILEGES ON ticketing_user.* TO 'ticketing'@'%';
GRANT ALL PRIVILEGES ON ticketing_event.* TO 'ticketing'@'%';
GRANT ALL PRIVILEGES ON ticketing_reservation.* TO 'ticketing'@'%';
GRANT ALL PRIVILEGES ON ticketing_payment.* TO 'ticketing'@'%';
GRANT ALL PRIVILEGES ON ticketing_booking.* TO 'ticketing'@'%';
GRANT ALL PRIVILEGES ON ticketing_notification.* TO 'ticketing'@'%';
FLUSH PRIVILEGES;

-- ────────────────────────── ticketing_user ──────────────────────────
USE ticketing_user;

CREATE TABLE IF NOT EXISTS users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    name       VARCHAR(100) NOT NULL,
    phone      VARCHAR(20),
    role       VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ────────────────────────── ticketing_event ──────────────────────────
USE ticketing_event;

CREATE TABLE IF NOT EXISTS events (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    venue            VARCHAR(255) NOT NULL,
    category         VARCHAR(50)  NOT NULL,
    description      TEXT,
    event_date       DATETIME     NOT NULL,
    booking_open_at  DATETIME     NOT NULL,
    booking_close_at DATETIME     NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED',
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_event_date (event_date),
    INDEX idx_status_open (status, booking_open_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS seats (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id  BIGINT      NOT NULL,
    zone      VARCHAR(50) NOT NULL,
    row_num   VARCHAR(10) NOT NULL,
    number    INT         NOT NULL,
    grade     VARCHAR(20) NOT NULL,
    price     INT         NOT NULL,
    status    VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    version   INT         NOT NULL DEFAULT 0,
    UNIQUE KEY uk_seat_position (event_id, zone, row_num, number),
    INDEX idx_event_status (event_id, status),
    CONSTRAINT fk_seat_event FOREIGN KEY (event_id) REFERENCES events (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ────────────────────────── ticketing_reservation ──────────────────────────
USE ticketing_reservation;

CREATE TABLE IF NOT EXISTS reservations (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    seat_id         BIGINT       NOT NULL,
    event_id        BIGINT       NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    expires_at      DATETIME     NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_idempotency (idempotency_key),
    INDEX idx_user_status (user_id, status),
    INDEX idx_seat_status (seat_id, status),
    INDEX idx_expires_status (expires_at, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS outbox_events (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    aggregate_type VARCHAR(50)  NOT NULL,
    aggregate_id   BIGINT       NOT NULL,
    event_type     VARCHAR(100) NOT NULL,
    payload        JSON         NOT NULL,
    published      TINYINT(1)   NOT NULL DEFAULT 0,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at   DATETIME,
    INDEX idx_unpublished (published, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ────────────────────────── ticketing_payment ──────────────────────────
USE ticketing_payment;

CREATE TABLE IF NOT EXISTS payments (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id    BIGINT       NOT NULL,
    user_id           BIGINT       NOT NULL,
    seat_id           BIGINT       NOT NULL,
    event_id          BIGINT       NOT NULL,
    amount            INT          NOT NULL,
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    payment_method    VARCHAR(50),
    pg_transaction_id VARCHAR(100),
    idempotency_key   VARCHAR(100) NOT NULL,
    failed_reason     VARCHAR(255),
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_idempotency (idempotency_key),
    UNIQUE KEY uk_pg_transaction (pg_transaction_id),
    INDEX idx_reservation (reservation_id),
    INDEX idx_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS outbox_events (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    aggregate_type VARCHAR(50)  NOT NULL,
    aggregate_id   BIGINT       NOT NULL,
    event_type     VARCHAR(100) NOT NULL,
    payload        JSON         NOT NULL,
    published      TINYINT(1)   NOT NULL DEFAULT 0,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at   DATETIME,
    INDEX idx_unpublished (published, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ────────────────────────── ticketing_booking ──────────────────────────
USE ticketing_booking;

CREATE TABLE IF NOT EXISTS bookings (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT       NOT NULL,
    payment_id     BIGINT       NOT NULL,
    user_id        BIGINT       NOT NULL,
    seat_id        BIGINT       NOT NULL,
    event_id       BIGINT       NOT NULL,
    booking_number VARCHAR(50)  NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'CONFIRMED',
    cancelled_at   DATETIME,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_reservation (reservation_id),
    UNIQUE KEY uk_payment (payment_id),
    UNIQUE KEY uk_booking_number (booking_number),
    INDEX idx_user_status (user_id, status),
    INDEX idx_event (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS outbox_events (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    aggregate_type VARCHAR(50)  NOT NULL,
    aggregate_id   BIGINT       NOT NULL,
    event_type     VARCHAR(100) NOT NULL,
    payload        JSON         NOT NULL,
    published      TINYINT(1)   NOT NULL DEFAULT 0,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at   DATETIME,
    INDEX idx_unpublished (published, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Consumer 멱등성 처리 테이블
CREATE TABLE IF NOT EXISTS processed_events (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id     VARCHAR(100) NOT NULL,
    processed_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_event_id (event_id),
    INDEX idx_processed_at (processed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ────────────────────────── ticketing_notification ──────────────────────────
USE ticketing_notification;

CREATE TABLE IF NOT EXISTS processed_events (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id     VARCHAR(100) NOT NULL,
    processed_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_event_id (event_id),
    INDEX idx_processed_at (processed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS notification_logs (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    type         VARCHAR(50)  NOT NULL,
    title        VARCHAR(255) NOT NULL,
    content      TEXT,
    status       VARCHAR(20)  NOT NULL DEFAULT 'SENT',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ────────────────────────── Sample Data ──────────────────────────
USE ticketing_user;
INSERT IGNORE INTO users (email, password, name, phone, role)
VALUES ('admin@ticketing.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', '010-0000-0000', 'ADMIN');

USE ticketing_event;
INSERT IGNORE INTO events (id, title, venue, category, description, event_date, booking_open_at, booking_close_at, status)
VALUES (1, '2024 콜드플레이 내한공연', '잠실 올림픽주경기장', 'CONCERT',
        '콜드플레이의 Music of the Spheres World Tour',
        DATE_ADD(NOW(), INTERVAL 30 DAY),
        NOW(),
        DATE_ADD(NOW(), INTERVAL 29 DAY), 'OPEN');

-- A구역 VIP 10석
INSERT IGNORE INTO seats (event_id, zone, row_num, number, grade, price, status)
SELECT 1, 'A', '1', n, 'VIP', 165000, 'AVAILABLE'
FROM (SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
      UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) nums;

-- B구역 R석 20석
INSERT IGNORE INTO seats (event_id, zone, row_num, number, grade, price, status)
SELECT 1, 'B', '1', n, 'R', 132000, 'AVAILABLE'
FROM (SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
      UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
      UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
      UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20) nums;
