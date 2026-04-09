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
