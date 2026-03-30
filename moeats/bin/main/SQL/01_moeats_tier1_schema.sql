
-- =========================================================
-- Mo-Eats Tier 1 Final Schema + Business Query Pack
-- 기준 문서 교차검증 반영본
-- - Tier 1 한 사이클 완주: 방 생성 -> 참여 -> 협업 -> 결제 -> 점주 처리 -> 조회
-- - payment_mode: REPRESENTATIVE / INDIVIDUAL
-- - participant_role: LEADER / PARTICIPANT
-- - selection_status 사용, menu_status(참여자용) 사용 금지
-- - 각자결제 5분 타이머 적용
-- - group_order / group_order_item 스냅샷은 결제 단계 진입 시 생성
-- =========================================================

CREATE DATABASE IF NOT EXISTS moeats
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE moeats;

-- =========================================================
-- 0. TABLES
-- =========================================================

CREATE TABLE IF NOT EXISTS member (
    member_idx INT AUTO_INCREMENT PRIMARY KEY,
    member_email VARCHAR(255) NOT NULL,
    member_password VARCHAR(255) NOT NULL,
    member_nickname VARCHAR(30) NOT NULL,
    member_phone VARCHAR(20) NOT NULL,
    member_role_type VARCHAR(5) NOT NULL,
    default_delivery_address_idx INT NULL,
    member_status VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_member_email UNIQUE (member_email),
    CONSTRAINT chk_member_role_type CHECK (member_role_type IN ('USER', 'OWNER')),
    CONSTRAINT chk_member_status CHECK (member_status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS delivery_address (
    delivery_address_idx INT AUTO_INCREMENT PRIMARY KEY,
    member_idx INT NOT NULL,
    delivery_label VARCHAR(100) NOT NULL,
    recipient_name VARCHAR(20) NOT NULL,
    recipient_phone VARCHAR(20) NOT NULL,
    zip_code VARCHAR(10) NOT NULL,
    delivery_address1 VARCHAR(100) NOT NULL,
    delivery_address2 VARCHAR(100) NOT NULL,
    delivery_request VARCHAR(100) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_delivery_address_member
        FOREIGN KEY (member_idx) REFERENCES member(member_idx)
) ENGINE=InnoDB;

ALTER TABLE member
    ADD CONSTRAINT fk_member_default_delivery_address
    FOREIGN KEY (default_delivery_address_idx)
    REFERENCES delivery_address(delivery_address_idx);

CREATE TABLE IF NOT EXISTS store (
    store_idx INT AUTO_INCREMENT PRIMARY KEY,
    owner_member_idx INT NOT NULL,
    store_name VARCHAR(30) NOT NULL,
    store_description TEXT NULL,
    store_phone VARCHAR(20) NOT NULL,
    minimum_order_amount INT NOT NULL DEFAULT 0,
    store_address1 VARCHAR(100) NOT NULL,
    store_address2 VARCHAR(100) NOT NULL,
    supports_delivery BOOLEAN NOT NULL,
    supports_onsite BOOLEAN NOT NULL,
    store_status VARCHAR(8) NOT NULL,
    longitude DECIMAL(9,6) NULL,
    latitude DECIMAL(10,6) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_store_status CHECK (store_status IN ('ACTIVE', 'INACTIVE', 'PAUSED')),
    CONSTRAINT fk_store_owner_member
        FOREIGN KEY (owner_member_idx) REFERENCES member(member_idx)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS store_menu (
    menu_idx INT AUTO_INCREMENT PRIMARY KEY,
    store_idx INT NOT NULL,
    menu_name VARCHAR(30) NOT NULL,
    menu_description TEXT NULL,
    menu_price INT NOT NULL,
    menu_status VARCHAR(9) NOT NULL,
    display_order INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_menu_status CHECK (menu_status IN ('AVAILABLE', 'SOLD_OUT', 'HIDDEN')),
    CONSTRAINT fk_store_menu_store
        FOREIGN KEY (store_idx) REFERENCES store(store_idx)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS menu_image (
    menu_image_idx INT AUTO_INCREMENT PRIMARY KEY,
    menu_idx INT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_menu_image_menu
        FOREIGN KEY (menu_idx) REFERENCES store_menu(menu_idx)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS order_room (
    room_idx INT AUTO_INCREMENT PRIMARY KEY,
    leader_member_idx INT NOT NULL,
    store_idx INT NOT NULL,
    selected_delivery_address_idx INT NULL,
    room_code VARCHAR(6) NOT NULL,
    order_mode VARCHAR(8) NOT NULL,
    payment_mode VARCHAR(14) NOT NULL,
    room_status VARCHAR(15) NOT NULL,
    is_join_locked BOOLEAN NOT NULL DEFAULT FALSE,
    locked_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_order_room_code UNIQUE (room_code),
    CONSTRAINT chk_order_room_mode CHECK (order_mode IN ('DELIVERY', 'ONSITE')),
    CONSTRAINT chk_order_room_payment_mode CHECK (payment_mode IN ('REPRESENTATIVE', 'INDIVIDUAL')),
    CONSTRAINT chk_order_room_status CHECK (
        room_status IN ('OPEN', 'SELECTING', 'PAYMENT_PENDING', 'ORDER_CONFIRMED', 'CANCELLED', 'EXPIRED')
    ),
    CONSTRAINT fk_order_room_leader_member
        FOREIGN KEY (leader_member_idx) REFERENCES member(member_idx),
    CONSTRAINT fk_order_room_store
        FOREIGN KEY (store_idx) REFERENCES store(store_idx),
    CONSTRAINT fk_order_room_selected_delivery_address
        FOREIGN KEY (selected_delivery_address_idx) REFERENCES delivery_address(delivery_address_idx)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS room_participant (
    room_participant_idx INT AUTO_INCREMENT PRIMARY KEY,
    room_idx INT NOT NULL,
    member_idx INT NOT NULL,
    participant_role VARCHAR(11) NOT NULL,
    selection_status VARCHAR(12) NOT NULL,
    payment_status VARCHAR(6) NOT NULL,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_room_participant UNIQUE (room_idx, member_idx),
    CONSTRAINT chk_participant_role CHECK (participant_role IN ('LEADER', 'PARTICIPANT')),
    CONSTRAINT chk_selection_status CHECK (selection_status IN ('NOT_SELECTED', 'SELECTED')),
    CONSTRAINT chk_room_participant_payment_status CHECK (payment_status IN ('UNPAID', 'PAID')),
    CONSTRAINT fk_room_participant_room
        FOREIGN KEY (room_idx) REFERENCES order_room(room_idx),
    CONSTRAINT fk_room_participant_member
        FOREIGN KEY (member_idx) REFERENCES member(member_idx)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS group_cart_item (
    cart_item_idx INT AUTO_INCREMENT PRIMARY KEY,
    room_idx INT NOT NULL,
    member_idx INT NOT NULL,
    menu_idx INT NOT NULL,
    item_quantity INT NOT NULL DEFAULT 1,
    base_amount INT NOT NULL,
    option_extra_amount INT NOT NULL DEFAULT 0,
    item_total_amount INT NOT NULL,
    item_status VARCHAR(7) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_group_cart_item_status CHECK (item_status IN ('ACTIVE', 'REMOVED')),
    CONSTRAINT fk_group_cart_item_room
        FOREIGN KEY (room_idx) REFERENCES order_room(room_idx),
    CONSTRAINT fk_group_cart_item_member
        FOREIGN KEY (member_idx) REFERENCES member(member_idx),
    CONSTRAINT fk_group_cart_item_menu
        FOREIGN KEY (menu_idx) REFERENCES store_menu(menu_idx)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS group_order (
    order_idx INT AUTO_INCREMENT PRIMARY KEY,
    room_idx INT NOT NULL,
    store_idx INT NOT NULL,
    leader_member_idx INT NOT NULL,
    order_mode VARCHAR(8) NOT NULL,
    payment_mode VARCHAR(14) NOT NULL,
    order_status VARCHAR(15) NOT NULL,
    order_total_amount INT NOT NULL,
    expected_visit_at TIMESTAMP NULL,
    payment_confirmed_at TIMESTAMP NULL,
    store_confirmed_at TIMESTAMP NULL,
    checked_in_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    cancelled_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_group_order_room UNIQUE (room_idx),
    CONSTRAINT chk_group_order_mode CHECK (order_mode IN ('DELIVERY', 'ONSITE')),
    CONSTRAINT chk_group_order_payment_mode CHECK (payment_mode IN ('REPRESENTATIVE', 'INDIVIDUAL')),
    CONSTRAINT chk_group_order_status CHECK (
        order_status IN (
            'PAYMENT_PENDING', 'PAID', 'ACCEPTED', 'PREPARING', 'READY',
            'DELIVERING', 'CHECKED_IN', 'COMPLETED', 'CANCELLED'
        )
    ),
    CONSTRAINT fk_group_order_room
        FOREIGN KEY (room_idx) REFERENCES order_room(room_idx),
    CONSTRAINT fk_group_order_store
        FOREIGN KEY (store_idx) REFERENCES store(store_idx),
    CONSTRAINT fk_group_order_leader_member
        FOREIGN KEY (leader_member_idx) REFERENCES member(member_idx)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS group_order_item (
    order_item_idx INT AUTO_INCREMENT PRIMARY KEY,
    order_idx INT NOT NULL,
    member_idx INT NOT NULL,
    menu_idx INT NOT NULL,
    menu_name_snapshot VARCHAR(30) NOT NULL,
    menu_price_snapshot INT NOT NULL,
    item_quantity INT NOT NULL,
    base_amount INT NOT NULL,
    option_extra_amount INT NOT NULL DEFAULT 0,
    item_total_amount INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_group_order_item_order
        FOREIGN KEY (order_idx) REFERENCES group_order(order_idx),
    CONSTRAINT fk_group_order_item_member
        FOREIGN KEY (member_idx) REFERENCES member(member_idx),
    CONSTRAINT fk_group_order_item_menu
        FOREIGN KEY (menu_idx) REFERENCES store_menu(menu_idx)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS payment (
    payment_idx INT AUTO_INCREMENT PRIMARY KEY,
    order_idx INT NOT NULL,
    payment_mode VARCHAR(14) NOT NULL,
    payment_request_amount INT NOT NULL,
    payment_paid_amount INT NOT NULL DEFAULT 0,
    payment_status VARCHAR(12) NOT NULL,
    payment_started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payment_expires_at TIMESTAMP NULL,
    paid_at TIMESTAMP NULL,
    cancelled_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_payment_order UNIQUE (order_idx),
    CONSTRAINT chk_payment_mode CHECK (payment_mode IN ('REPRESENTATIVE', 'INDIVIDUAL')),
    CONSTRAINT chk_payment_status CHECK (payment_status IN ('READY', 'IN_PROGRESS', 'PAID', 'CANCELLED')),
    CONSTRAINT fk_payment_order
        FOREIGN KEY (order_idx) REFERENCES group_order(order_idx)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS payment_share (
    payment_share_idx INT AUTO_INCREMENT PRIMARY KEY,
    payment_idx INT NOT NULL,
    member_idx INT NOT NULL,
    share_amount INT NOT NULL,
    pay_method VARCHAR(30) NULL,
    share_status VARCHAR(22) NOT NULL,
    paid_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_payment_share UNIQUE (payment_idx, member_idx),
    CONSTRAINT chk_payment_share_status CHECK (
        share_status IN ('PENDING', 'PAID_SELF', 'PAID_BY_REPRESENTATIVE', 'CANCELLED')
    ),
    CONSTRAINT fk_payment_share_payment
        FOREIGN KEY (payment_idx) REFERENCES payment(payment_idx),
    CONSTRAINT fk_payment_share_member
        FOREIGN KEY (member_idx) REFERENCES member(member_idx)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS order_delivery (
    order_delivery_idx INT AUTO_INCREMENT PRIMARY KEY,
    order_idx INT NOT NULL,
    source_delivery_address_idx INT NOT NULL,
    recipient_name VARCHAR(20) NOT NULL,
    recipient_phone VARCHAR(20) NOT NULL,
    zip_code VARCHAR(10) NOT NULL,
    delivery_address1 VARCHAR(100) NOT NULL,
    delivery_address2 VARCHAR(100) NOT NULL,
    delivery_request VARCHAR(100) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_order_delivery_order UNIQUE (order_idx),
    CONSTRAINT fk_order_delivery_order
        FOREIGN KEY (order_idx) REFERENCES group_order(order_idx),
    CONSTRAINT fk_order_delivery_source_address
        FOREIGN KEY (source_delivery_address_idx) REFERENCES delivery_address(delivery_address_idx)
) ENGINE=InnoDB;

-- =========================================================
-- 1. INDEXES
-- =========================================================

CREATE INDEX idx_delivery_address_member_active
    ON delivery_address (member_idx, is_active, updated_at);

CREATE INDEX idx_store_status
    ON store (store_status, owner_member_idx, created_at);

CREATE INDEX idx_store_menu_store_status_order
    ON store_menu (store_idx, menu_status, display_order);

CREATE INDEX idx_menu_image_menu_primary
    ON menu_image (menu_idx, is_primary, display_order);

CREATE INDEX idx_order_room_leader_status
    ON order_room (leader_member_idx, room_status, updated_at);

CREATE INDEX idx_order_room_store_status
    ON order_room (store_idx, room_status, updated_at);

CREATE INDEX idx_room_participant_room
    ON room_participant (room_idx, participant_role, selection_status, payment_status);

CREATE INDEX idx_room_participant_member
    ON room_participant (member_idx, payment_status, updated_at);

CREATE INDEX idx_group_cart_item_room_member_status
    ON group_cart_item (room_idx, member_idx, item_status, updated_at);

CREATE INDEX idx_group_order_store_status
    ON group_order (store_idx, order_status, created_at);

CREATE INDEX idx_group_order_leader_status
    ON group_order (leader_member_idx, order_status, created_at);

CREATE INDEX idx_group_order_item_order_member
    ON group_order_item (order_idx, member_idx);

CREATE INDEX idx_payment_status
    ON payment (payment_status, payment_started_at, payment_expires_at);

CREATE INDEX idx_payment_share_payment_status
    ON payment_share (payment_idx, share_status);

-- =========================================================
