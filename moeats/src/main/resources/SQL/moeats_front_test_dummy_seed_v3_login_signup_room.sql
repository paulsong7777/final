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

USE moeats;

START TRANSACTION;

-- =========================================================
-- 0. MEMBER
--   주의: default_delivery_address_idx 는 주소 INSERT 후 UPDATE 한다.
-- =========================================================
INSERT INTO member (
    member_idx,
    member_email,
    member_password,
    member_nickname,
    member_phone,
    member_role_type,
    default_delivery_address_idx,
    member_status,
    created_at,
    updated_at
) VALUES
(81101, 'user01@mo.eats', '1234', '유저01', '010-1100-0001', 'USER', NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(81102, 'user02@mo.eats', '1234', '유저02', '010-1100-0002', 'USER', NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(81103, 'user03@mo.eats', '1234', '유저03', '010-1100-0003', 'USER', NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(81104, 'user04@mo.eats', '1234', '유저04', '010-1100-0004', 'USER', NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(81105, 'user05@mo.eats', '1234', '유저05', '010-1100-0005', 'USER', NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(81106, 'test@te.st', 'test', '테스트유저', '010-1100-0099', 'USER', NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(81201, 'owner01@mo.eats', '1234', '점주01', '010-2200-0001', 'OWNER', NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(81202, 'owner02@mo.eats', '1234', '점주02', '010-2200-0002', 'OWNER', NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(81203, 'owner03@mo.eats', '1234', '점주03', '010-2200-0003', 'OWNER', NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(81204, 'owner04@mo.eats', '1234', '점주04', '010-2200-0004', 'OWNER', NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(81205, 'owner05@mo.eats', '1234', '점주05', '010-2200-0005', 'OWNER', NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- =========================================================
-- 1. DELIVERY_ADDRESS
--   USER 계정과 test 계정만 주소를 넣는다.
-- =========================================================
INSERT INTO delivery_address (
    delivery_address_idx,
    member_idx,
    delivery_label,
    recipient_name,
    recipient_phone,
    zip_code,
    delivery_address1,
    delivery_address2,
    longitude,
    latitude,
    delivery_request,
    is_active,
    created_at,
    updated_at
) VALUES
(82101, 81101, '집', '유저01', '010-1100-0001', '41911', '대구광역시 중구 국채보상로 600', '101동 1001호', 128.593900, 35.869900, '문 앞에 놓아주세요', TRUE, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(82102, 81102, '집', '유저02', '010-1100-0002', '41913', '대구광역시 중구 중앙대로 390', '202호', 128.593600, 35.869400, '도착 전에 연락 주세요', TRUE, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(82103, 81103, '집', '유저03', '010-1100-0003', '41942', '대구광역시 중구 동성로2길 50', '3층', 128.596100, 35.868300, '벨 한 번만 눌러주세요', TRUE, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(82104, 81104, '집', '유저04', '010-1100-0004', '41968', '대구광역시 중구 달구벌대로 2109', '오피스텔 1204호', 128.588800, 35.864800, '경비실에 맡겨주세요', TRUE, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(82105, 81105, '집', '유저05', '010-1100-0005', '41958', '대구광역시 중구 남산로 70', '501호', 128.589900, 35.859900, '문 앞에 두고 가주세요', TRUE, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(82106, 81106, '테스트', '테스트유저', '010-1100-0099', '41926', '대구광역시 중구 큰장로 28', '2층', 128.582800, 35.866700, '빠른 테스트용 주소', TRUE, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 1-1. USER 기본 배송지 연결
UPDATE member SET default_delivery_address_idx = 82101 WHERE member_idx = 81101;
UPDATE member SET default_delivery_address_idx = 82102 WHERE member_idx = 81102;
UPDATE member SET default_delivery_address_idx = 82103 WHERE member_idx = 81103;
UPDATE member SET default_delivery_address_idx = 82104 WHERE member_idx = 81104;
UPDATE member SET default_delivery_address_idx = 82105 WHERE member_idx = 81105;
UPDATE member SET default_delivery_address_idx = 82106 WHERE member_idx = 81106;

-- =========================================================
-- 2. STORE
--   OWNER 1계정당 1매장으로 고정
-- =========================================================
INSERT INTO store (
    store_idx,
    owner_member_idx,
    store_name,
    store_category,
    store_description,
    store_phone,
    minimum_order_amount,
    store_address1,
    store_address2,
    supports_delivery,
    delivery_radius_m,
    supports_onsite,
    store_status,
    longitude,
    latitude,
    created_at,
    updated_at
) VALUES
(83101, 81201, '중구치킨', 'CHICKEN', '치킨 메뉴 확인용 매장', '053-601-0001', 15000, '대구광역시 중구 동성로 36', '1층', TRUE, 2200, FALSE, 'ACTIVE', 128.596700, 35.869200, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(83102, 81202, '중구피자', 'PIZZA', '피자 메뉴 확인용 매장', '053-601-0002', 18000, '대구광역시 중구 중앙대로 406', '1층', TRUE, 2200, TRUE, 'ACTIVE', 128.594500, 35.869100, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(83103, 81203, '중구중식', 'CHINESE', '중식 메뉴 확인용 매장', '053-601-0003', 12000, '대구광역시 중구 국채보상로 570', '1층', TRUE, 2200, FALSE, 'ACTIVE', 128.591800, 35.869100, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(83104, 81204, '중구한식', 'KOREAN', '한식 메뉴 확인용 매장', '053-601-0004', 10000, '대구광역시 중구 남산로 85', '1층', TRUE, 2200, TRUE, 'ACTIVE', 128.590200, 35.860500, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(83105, 81205, '중구카페', 'CAFE', '카페 메뉴 확인용 매장', '053-601-0005', 8000, '대구광역시 중구 달성로 45', '2층', TRUE, 2200, TRUE, 'ACTIVE', 128.583700, 35.867200, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- =========================================================
-- 3. STORE_MENU_CATEGORY
-- =========================================================
INSERT INTO store_menu_category (
    menu_category_idx,
    store_idx,
    category_name,
    display_order
) VALUES
(84101, 83101, '치킨', 1),
(84102, 83101, '사이드', 2),
(84103, 83101, '음료', 3),
(84104, 83102, '피자', 1),
(84105, 83102, '사이드', 2),
(84106, 83102, '음료', 3),
(84107, 83103, '대표메뉴', 1),
(84108, 83103, '면류', 2),
(84109, 83103, '음료', 3),
(84110, 83104, '식사', 1),
(84111, 83104, '찌개', 2),
(84112, 83104, '음료', 3),
(84113, 83105, '커피', 1),
(84114, 83105, '디저트', 2),
(84115, 83105, '음료', 3);

-- =========================================================
-- 4. STORE_MENU
--   주문방 생성 데모 전 가게 상세/메뉴 목록 확인용
-- =========================================================
INSERT INTO store_menu (
    menu_idx,
    store_idx,
    menu_category_idx,
    menu_name,
    menu_description,
    menu_price,
    menu_status,
    display_order,
    created_at,
    updated_at
) VALUES
(85101, 83101, 84101, '후라이드치킨', '후라이드치킨 메뉴', 19000, 'AVAILABLE', 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(85102, 83101, 84101, '양념치킨', '양념치킨 메뉴', 21000, 'AVAILABLE', 2, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(85103, 83101, 84102, '치즈볼', '치즈볼 메뉴', 5000, 'AVAILABLE', 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(85104, 83101, 84103, '콜라', '콜라 메뉴', 3000, 'AVAILABLE', 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(85105, 83102, 84104, '페퍼로니피자', '페퍼로니피자 메뉴', 23900, 'AVAILABLE', 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(85106, 83102, 84104, '불고기피자', '불고기피자 메뉴', 25900, 'AVAILABLE', 2, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(85107, 83102, 84105, '감자튀김', '감자튀김 메뉴', 5000, 'AVAILABLE', 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(85108, 83102, 84106, '제로콜라', '제로콜라 메뉴', 2500, 'AVAILABLE', 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(85109, 83103, 84107, '탕수육', '탕수육 메뉴', 18000, 'AVAILABLE', 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(85110, 83103, 84108, '짜장면', '짜장면 메뉴', 7000, 'AVAILABLE', 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(85111, 83103, 84108, '짬뽕', '짬뽕 메뉴', 9000, 'AVAILABLE', 2, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(85112, 83103, 84109, '사이다', '사이다 메뉴', 2000, 'AVAILABLE', 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(85113, 83104, 84110, '제육덮밥', '제육덮밥 메뉴', 11000, 'AVAILABLE', 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(85114, 83104, 84110, '비빔밥', '비빔밥 메뉴', 10000, 'AVAILABLE', 2, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(85115, 83104, 84111, '김치찌개', '김치찌개 메뉴', 9000, 'AVAILABLE', 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(85116, 83104, 84112, '식혜', '식혜 메뉴', 2500, 'AVAILABLE', 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(85117, 83105, 84113, '아메리카노', '아메리카노 메뉴', 3500, 'AVAILABLE', 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(85118, 83105, 84113, '카페라떼', '카페라떼 메뉴', 4500, 'AVAILABLE', 2, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(85119, 83105, 84114, '크로플', '크로플 메뉴', 6500, 'AVAILABLE', 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(85120, 83105, 84115, '레몬에이드', '레몬에이드 메뉴', 5500, 'AVAILABLE', 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- =========================================================
-- 5. MENU_IMAGE
--   메뉴카드 화면 확인용 대표이미지 1장씩만 넣는다.
-- =========================================================
INSERT INTO menu_image (
    menu_image_idx,
    menu_idx,
    image_url,
    is_primary,
    display_order,
    created_at
) VALUES
(86101, 85101, 'https://picsum.photos/seed/moeats-v3-85101/640/480', TRUE, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(86102, 85102, 'https://picsum.photos/seed/moeats-v3-85102/640/480', TRUE, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(86103, 85103, 'https://picsum.photos/seed/moeats-v3-85103/640/480', TRUE, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(86104, 85104, 'https://picsum.photos/seed/moeats-v3-85104/640/480', TRUE, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(86105, 85105, 'https://picsum.photos/seed/moeats-v3-85105/640/480', TRUE, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(86106, 85106, 'https://picsum.photos/seed/moeats-v3-85106/640/480', TRUE, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(86107, 85107, 'https://picsum.photos/seed/moeats-v3-85107/640/480', TRUE, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(86108, 85108, 'https://picsum.photos/seed/moeats-v3-85108/640/480', TRUE, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(86109, 85109, 'https://picsum.photos/seed/moeats-v3-85109/640/480', TRUE, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(86110, 85110, 'https://picsum.photos/seed/moeats-v3-85110/640/480', TRUE, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(86111, 85111, 'https://picsum.photos/seed/moeats-v3-85111/640/480', TRUE, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(86112, 85112, 'https://picsum.photos/seed/moeats-v3-85112/640/480', TRUE, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(86113, 85113, 'https://picsum.photos/seed/moeats-v3-85113/640/480', TRUE, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(86114, 85114, 'https://picsum.photos/seed/moeats-v3-85114/640/480', TRUE, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(86115, 85115, 'https://picsum.photos/seed/moeats-v3-85115/640/480', TRUE, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(86116, 85116, 'https://picsum.photos/seed/moeats-v3-85116/640/480', TRUE, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(86117, 85117, 'https://picsum.photos/seed/moeats-v3-85117/640/480', TRUE, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(86118, 85118, 'https://picsum.photos/seed/moeats-v3-85118/640/480', TRUE, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(86119, 85119, 'https://picsum.photos/seed/moeats-v3-85119/640/480', TRUE, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(86120, 85120, 'https://picsum.photos/seed/moeats-v3-85120/640/480', TRUE, 1, DATE_SUB(NOW(), INTERVAL 10 DAY));

-- =========================================================
-- 6. 이번 버전에서 의도적으로 제외한 데이터
--   - order_room
--   - room_participant
--   - group_cart_item
--   - group_order
--   - payment
--   - payment_share
--   - order_delivery
-- =========================================================

-- v3 더미데이터용 BCrypt 비밀번호 패치
-- 실행 전 아래 USE 구문을 실제 사용하는 DB명으로 바꿔서 실행하세요.
-- 예: USE moeats;
USE moeats;

UPDATE member
SET member_password = '$2y$10$KAIXFWqhmhvwNzGrVnwWvOJ/hwHftB35SGDCpe2Fm.bJi5JTCdzq6'
WHERE member_email IN (
    'user01@mo.eats', 'user02@mo.eats', 'user03@mo.eats', 'user04@mo.eats', 'user05@mo.eats',
    'owner01@mo.eats', 'owner02@mo.eats', 'owner03@mo.eats', 'owner04@mo.eats', 'owner05@mo.eats'
);

UPDATE member
SET member_password = '$2y$10$VA9j2OoQ6AKaTUPuwcv52ewfidRLKBwnseNH.gpImModwpK1ulLn.'
WHERE member_email = 'test@te.st';

SELECT member_email, member_password
FROM member
WHERE member_email IN (
    'user01@mo.eats', 'user02@mo.eats', 'user03@mo.eats', 'user04@mo.eats', 'user05@mo.eats',
    'owner01@mo.eats', 'owner02@mo.eats', 'owner03@mo.eats', 'owner04@mo.eats', 'owner05@mo.eats',
    'test@te.st'
)
ORDER BY member_email;


COMMIT;
