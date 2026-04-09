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
