
DROP TABLE IF EXISTS memo;
CREATE TABLE IF NOT EXISTS memo(
	no INTEGER AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(30) NOT NULL,
    content VARCHAR(500) NOT NULL,
    writer VARCHAR(20) NOT NULL,
    reg_date DATE NOT NULL
);

INSERT INTO memo(title, content, writer, reg_date) VALUES('프로젝트 기획안', '프로젝트 기획 초안 작성', 'admin', NOW());
INSERT INTO memo(title, content, writer, reg_date) VALUES('프로젝트 기획 발표 준비', '프로젝트 기획 회의완료 후 발표 준비', 'servlet', NOW());
INSERT INTO memo(title, content, writer, reg_date) VALUES('오늘 할일 정리', '오늘 아침 거르고 점심 맛나게 먹고 저녁은?', 'spring', NOW());

commit;
SELECT * FROM memo;


