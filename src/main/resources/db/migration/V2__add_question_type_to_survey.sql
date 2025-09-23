-- 설문 질문 테이블에 질문 타입 필드 추가
ALTER TABLE survey_questions
ADD COLUMN question_type VARCHAR(20) NOT NULL DEFAULT 'SINGLE_CHOICE';

-- 1~4번 문항을 다중 선택으로 업데이트
UPDATE survey_questions
SET question_type = 'MULTIPLE_CHOICE'
WHERE question_number BETWEEN 1 AND 4;

-- 나머지 문항(5~13번)은 단일 선택 유지
UPDATE survey_questions
SET question_type = 'SINGLE_CHOICE'
WHERE question_number BETWEEN 5 AND 13;

-- 새로운 설문 문항 데이터 삽입 (예시)
-- 1번 문항: 중요하거나 어려운 일이 있을 때 조언을 구할 수 있는 사람이 있나요?
UPDATE survey_questions
SET question_text = '중요하거나 어려운 일이 있을 때 조언을 구할 수 있는 사람이 있나요?'
WHERE question_number = 1;

UPDATE survey_question_options SET option_text = '가족, 친척' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 1) AND option_number = 1;
UPDATE survey_question_options SET option_text = '친구' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 1) AND option_number = 2;
UPDATE survey_question_options SET option_text = '기타 타인 (이웃, 직장동료 등)' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 1) AND option_number = 3;
UPDATE survey_question_options SET option_text = '없음' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 1) AND option_number = 4;

-- 2번 문항: 급한 일을 부탁할 사람이 있나요?
UPDATE survey_questions
SET question_text = '급한 일을 부탁할 사람이 있나요?'
WHERE question_number = 2;

UPDATE survey_question_options SET option_text = '가족, 친척' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 2) AND option_number = 1;
UPDATE survey_question_options SET option_text = '친구' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 2) AND option_number = 2;
UPDATE survey_question_options SET option_text = '기타 타인 (이웃, 직장동료 등)' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 2) AND option_number = 3;
UPDATE survey_question_options SET option_text = '없음' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 2) AND option_number = 4;

-- 3번 문항: 돈을 빌려야 할 때 부탁할 사람이 있나요?
UPDATE survey_questions
SET question_text = '돈을 빌려야 할 때 부탁할 사람이 있나요?'
WHERE question_number = 3;

UPDATE survey_question_options SET option_text = '가족, 친척' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 3) AND option_number = 1;
UPDATE survey_question_options SET option_text = '친구' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 3) AND option_number = 2;
UPDATE survey_question_options SET option_text = '기타 타인 (이웃, 직장동료 등)' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 3) AND option_number = 3;
UPDATE survey_question_options SET option_text = '없음' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 3) AND option_number = 4;

-- 4번 문항: 속마음을 털어놓고 얘기할 사람이 있나요?
UPDATE survey_questions
SET question_text = '속마음을 털어놓고 얘기할 사람이 있나요?'
WHERE question_number = 4;

UPDATE survey_question_options SET option_text = '가족, 친척' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 4) AND option_number = 1;
UPDATE survey_question_options SET option_text = '친구' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 4) AND option_number = 2;
UPDATE survey_question_options SET option_text = '기타 타인 (이웃, 직장동료 등)' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 4) AND option_number = 3;
UPDATE survey_question_options SET option_text = '없음' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 4) AND option_number = 4;

-- 5번 문항부터는 단일 선택
UPDATE survey_questions
SET question_text = '가족들과의 대면 교류 주기는 어떻게 되나요?'
WHERE question_number = 5;

UPDATE survey_question_options SET option_text = '전혀, 거의 없다' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 5) AND option_number = 1;
UPDATE survey_question_options SET option_text = '몇개월에 한번' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 5) AND option_number = 2;
UPDATE survey_question_options SET option_text = '한달에 한번' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 5) AND option_number = 3;
UPDATE survey_question_options SET option_text = '일주일에 한번' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 5) AND option_number = 4;
UPDATE survey_question_options SET option_text = '일주일에 여러번' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 5) AND option_number = 5;