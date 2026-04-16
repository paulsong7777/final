-- =========================================================
-- Mo-Eats 전체 초기화 + 기준선 재적재 + v3 더미 재적재 올인원
-- 실행 순서:
--   이 파일 하나만 전체 실행
-- 용도:
--   더미/테스트 데이터가 꼬였을 때 DB를 통째로 초기화하고
--   01~04 기준선 + v3 시드 + bcrypt 패치까지 한 번에 다시 올린다.
-- =========================================================

DROP DATABASE IF EXISTS moeats;
CREATE DATABASE moeats
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
USE moeats;



-- =========================================================
-- 01_moeats_tier1_schema.sql
-- =========================================================

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

ALTER TABLE menu_image
    MODIFY COLUMN image_url VARCHAR(2048) NOT NULL;

ALTER TABLE store_menu DROP CHECK chk_menu_status;

ALTER TABLE store_menu ADD CONSTRAINT chk_menu_status 
CHECK (menu_status IN ('AVAILABLE', 'SOLD_OUT', 'HIDDEN', 'DELETED'));
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


-- =========================================================
-- 02_store_category_migration.sql
-- =========================================================
-- =========================================================
-- 02_store_category_migration.sql
-- 목적:
--   store 테이블이 비어 있는 현재 상태를 기준으로
--   store_category 컬럼 / CHECK 제약 / 인덱스를 추가한다.
--
-- 최종 카테고리 코드값:
--   CHICKEN / PIZZA / CHINESE / KOREAN / CAFE
-- =========================================================

USE moeats;

ALTER TABLE store
    ADD COLUMN store_category VARCHAR(20) NOT NULL AFTER store_name;

ALTER TABLE store
    ADD CONSTRAINT chk_store_category
    CHECK (store_category IN ('CHICKEN', 'PIZZA', 'CHINESE', 'KOREAN', 'CAFE'));

CREATE INDEX idx_store_category_status
    ON store (store_category, store_status, created_at);

-- =========================================================
-- INSERT 예시
-- =========================================================
-- INSERT INTO store (
--     owner_member_idx,
--     store_name,
--     store_category,
--     store_description,
--     store_phone,
--     minimum_order_amount,
--     store_address1,
--     store_address2,
--     supports_delivery,
--     supports_onsite,
--     store_status,
--     longitude,
--     latitude
-- ) VALUES (
--     1,
--     '교촌치킨',
--     'CHICKEN',
--     '바삭한 후라이드와 양념치킨 전문점',
--     '053-123-4567',
--     15000,
--     '대구광역시 수성구 달구벌대로 123',
--     '1층',
--     TRUE,
--     FALSE,
--     'ACTIVE',
--     128.623100,
--     35.857200
-- );


-- =========================================================
-- 03_store_menu_category_.sql
-- =========================================================
-- =========================================================
-- 03_store_menu_category_migration_v2.sql
-- 목적:
--   업주가 가게별 메뉴 카테고리를 직접 등록하고,
--   store_menu가 해당 카테고리를 선택해서 연결할 수 있도록
--   메뉴 카테고리 테이블과 외래키 컬럼을 추가한다.
--
-- 기준:
--   - store_category는 가게 업종 분류용으로 유지한다.
--   - store_menu_category는 가게 내부 메뉴 섹션용으로 사용한다.
--   - store_menu.store_idx는 기존대로 유지한다.
--   - store_menu.menu_category_idx는 초기 마이그레이션 단계에서는 NULL 허용으로 추가한다.
--     (기존 메뉴 데이터에 카테고리를 매핑한 뒤 필요 시 NOT NULL 전환)
-- =========================================================

USE moeats;

-- =========================================================
-- 1. 메뉴 카테고리 마스터 테이블 생성
-- =========================================================
CREATE TABLE IF NOT EXISTS store_menu_category (
    menu_category_idx INT AUTO_INCREMENT PRIMARY KEY,
    store_idx INT NOT NULL,
    category_name VARCHAR(30) NOT NULL,
    display_order INT NOT NULL DEFAULT 1,

    CONSTRAINT uk_store_menu_category UNIQUE (store_idx, category_name),
    CONSTRAINT chk_store_menu_category_name CHECK (CHAR_LENGTH(TRIM(category_name)) > 0),
    CONSTRAINT fk_store_menu_category_store
        FOREIGN KEY (store_idx) REFERENCES store(store_idx)
) ENGINE=InnoDB;

-- =========================================================
-- 2. store_menu에 메뉴 카테고리 연결 컬럼 추가
-- =========================================================
ALTER TABLE store_menu
    ADD COLUMN menu_category_idx INT NULL AFTER store_idx;

ALTER TABLE store_menu
    ADD CONSTRAINT fk_store_menu_menu_category
    FOREIGN KEY (menu_category_idx)
    REFERENCES store_menu_category(menu_category_idx);

-- =========================================================
-- 3. 인덱스 추가
-- =========================================================
CREATE INDEX idx_store_menu_category_store_order
    ON store_menu_category (store_idx, display_order, menu_category_idx);

CREATE INDEX idx_store_menu_store_category_status_order
    ON store_menu (store_idx, menu_category_idx, menu_status, display_order, menu_idx);

-- =========================================================
-- 4. 운영 메모
-- =========================================================
-- [메뉴 등록/수정 시 필수 검증]
-- 1) menu_category_idx가 선택된 경우,
--    해당 카테고리의 store_idx와 저장 대상 menu의 store_idx가 같은지 서비스 로직에서 검증한다.
-- 2) 카테고리 삭제는 연결된 메뉴가 없을 때만 물리 삭제하도록 처리한다.
--
-- [기존 메뉴 데이터가 있는 경우 권장 순서]
-- 1) 카테고리 생성
-- 2) 기존 store_menu.menu_category_idx 매핑 UPDATE
-- 3) 모든 메뉴가 매핑된 뒤 필요 시 NOT NULL 전환 검토
--
-- 예시:
-- ALTER TABLE store_menu
--     MODIFY COLUMN menu_category_idx INT NOT NULL;
-- =========================================================

-- =========================================================
-- 5. 예시 데이터
-- =========================================================
-- INSERT INTO store_menu_category (store_idx, category_name, display_order)
-- VALUES
--   (1, '치킨', 1),
--   (1, '순살', 2),
--   (1, '음료', 3),
--   (1, '주류', 4),
--   (1, '사이드메뉴', 5);
--
-- UPDATE store_menu
--    SET menu_category_idx = 1
--  WHERE store_idx = 1
--    AND menu_name IN ('양념', '후라이드', '반반');
-- =========================================================


-- =========================================================
-- 04_delivery_address_geo_visibility_migration.sql
-- =========================================================
-- =========================================================
-- 04_delivery_address_geo_visibility_migration.sql
-- 목적:
--   헤더에서 선택한 배송지 기준으로
--   배달 가능한 가게만 노출하기 위한 최소 DB 반영
--
-- 기준:
--   1) 기본 배송지는 member.default_delivery_address_idx 유지
--   2) order_room.selected_delivery_address_idx 기존 구조 유지
--   3) store.longitude / store.latitude 기존 컬럼 활용
--   4) 새로 추가하는 것은 delivery_address 좌표, store 배달 반경
-- =========================================================

USE moeats;

-- =========================================================
-- 1. delivery_address에 좌표 컬럼 추가
-- =========================================================
ALTER TABLE delivery_address
    ADD COLUMN longitude DECIMAL(9,6) NULL AFTER delivery_address2,
    ADD COLUMN latitude DECIMAL(10,6) NULL AFTER longitude;

-- =========================================================
-- 2. store에 가게별 배달 반경(m) 컬럼 추가
--    기본값은 2000m
-- =========================================================
ALTER TABLE store
    ADD COLUMN delivery_radius_m INT NOT NULL DEFAULT 2000 AFTER supports_delivery;

-- =========================================================
-- 3. CHECK 제약 추가
--    좌표 범위와 반경값 최소 검증
-- =========================================================
ALTER TABLE delivery_address
    ADD CONSTRAINT chk_delivery_address_longitude
        CHECK (longitude IS NULL OR (longitude BETWEEN -180 AND 180)),
    ADD CONSTRAINT chk_delivery_address_latitude
        CHECK (latitude IS NULL OR (latitude BETWEEN -90 AND 90));

ALTER TABLE store
    ADD CONSTRAINT chk_store_delivery_radius_m
        CHECK (delivery_radius_m > 0);

-- =========================================================
-- 4. 조회 성능용 인덱스 추가
-- =========================================================
CREATE INDEX idx_delivery_address_member_geo
    ON delivery_address (member_idx, is_active, latitude, longitude);

CREATE INDEX idx_store_delivery_visibility
    ON store (supports_delivery, store_status, delivery_radius_m, latitude, longitude);

-- =========================================================
-- 5. 운영 메모
-- =========================================================
-- [필수 후속 작업]
-- 1) 기존 delivery_address 데이터에 대해 주소 -> 좌표 변환 후
--    longitude / latitude 값을 채워야 한다.
-- 2) 기존 store 데이터도 longitude / latitude 값이 비어 있으면 채워야 한다.
-- 3) 좌표가 없는 배송지 또는 가게는 거리 계산 대상에서 제외한다.
--
-- [가게 목록 노출 기준]
-- - store_status = 'ACTIVE'
-- - supports_delivery = TRUE
-- - 배송지 좌표 존재
-- - 가게 좌표 존재
-- - 거리 <= delivery_radius_m
-- =========================================================


-- =========================================================
-- moeats_front_test_dummy_seed_v3_login_signup_room.sql
-- =========================================================
-- =========================================================
-- Mo-Eats Front Test Dummy Seed v3
-- 목적:
--   로그인 / 회원가입 / 주문방 생성 데모용 최소 더미데이터
--
-- 기준선:
--   1) 01_moeats_tier1_schema.sql
--   2) 02_store_category_migration.sql
--   3) 03_store_menu_category_migration_v2.sql
--   4) 04_delivery_address_geo_visibility_migration.sql
--
-- 이번 버전 반영:
--   - USER 5개 + 빠른 로그인용 test 계정 1개
--   - OWNER 5개 + 1계정당 1매장
--   - 주소 / 매장 위치 모두 대구광역시 중구 기준
--   - 주문방 / 주문 / 결제 더미는 의도적으로 제외
--   - 기존 v1/v2와 충돌하지 않도록 ID 대역을 분리했다.
--
-- 비밀번호:
--   - 일반 계정: 1234
--   - test 계정: test
-- =========================================================

