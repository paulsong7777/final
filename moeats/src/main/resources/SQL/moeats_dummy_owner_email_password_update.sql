USE moeats;

-- 원본 더미 점주(owner_...@moeats.local)만 변경
-- owner01@mo.eats ~ owner05@mo.eats 는 건드리지 않음
-- 비밀번호는 BCrypt(1234)로 통일

UPDATE member m
JOIN (
    SELECT
        member_idx,
        ROW_NUMBER() OVER (ORDER BY member_idx) AS rn
    FROM member
    WHERE member_role_type = 'OWNER'
      AND member_email LIKE 'owner\_%@moeats.local'
) t
  ON t.member_idx = m.member_idx
SET
    m.member_email = CONCAT('dummy', LPAD(t.rn, 3, '0'), '@mo.eats'),
    m.member_password = '$2y$10$KAIXFWqhmhvwNzGrVnwWvOJ/hwHftB35SGDCpe2Fm.bJi5JTCdzq6';

-- 확인용
SELECT
    m.member_idx,
    s.store_idx,
    s.store_name,
    m.member_email,
    m.member_password
FROM member m
JOIN store s
  ON s.owner_member_idx = m.member_idx
WHERE m.member_role_type = 'OWNER'
  AND m.member_email LIKE 'dummy%@mo.eats'
ORDER BY s.store_name, m.member_idx;
