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
