-- 카테고리 초기 데이터
INSERT INTO categories (name, created_at, modified_at) VALUES
('지원 사업', NOW(), NOW()),
('마음 챙김', NOW(), NOW()),
('생활 습관', NOW(), NOW()),
('방문지 추천', NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 테스트 계정 (공모전 심사용)
INSERT INTO members (oauth_id, email, role, nickname, profile_image_url, clover_count)
VALUES ('DEMO_USER_001', 'demo@live-contest.com', 'USER', '심사위원', NULL, 0)
ON DUPLICATE KEY UPDATE oauth_id = VALUES(oauth_id); 