-- 카테고리 초기 데이터
INSERT INTO categories (name, created_at, modified_at) VALUES 
('지원 사업', NOW(), NOW()),
('마음 챙김', NOW(), NOW()),
('생활 습관', NOW(), NOW()),
('방문지 추천', NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name); 