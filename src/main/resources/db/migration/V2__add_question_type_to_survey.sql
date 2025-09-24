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

-- 6번 문항: 친인척들과의 대면 교류 주기는 어떻게 되나요?
UPDATE survey_questions
SET question_text = '친인척들과의 대면 교류 주기는 어떻게 되나요?'
WHERE question_number = 6;

UPDATE survey_question_options SET option_text = '전혀, 거의 없다' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 6) AND option_number = 1;
UPDATE survey_question_options SET option_text = '몇개월에 한번' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 6) AND option_number = 2;
UPDATE survey_question_options SET option_text = '한달에 한번' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 6) AND option_number = 3;
UPDATE survey_question_options SET option_text = '일주일에 한번' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 6) AND option_number = 4;
UPDATE survey_question_options SET option_text = '일주일에 여러번' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 6) AND option_number = 5;

-- 7번 문항: 친한 친구/지인 교류 주기는 어떻게 되나요?
UPDATE survey_questions
SET question_text = '친한 친구/지인 교류 주기는 어떻게 되나요?'
WHERE question_number = 7;

UPDATE survey_question_options SET option_text = '전혀, 거의 없다' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 7) AND option_number = 1;
UPDATE survey_question_options SET option_text = '몇개월에 한번' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 7) AND option_number = 2;
UPDATE survey_question_options SET option_text = '한달에 한번' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 7) AND option_number = 3;
UPDATE survey_question_options SET option_text = '일주일에 한번' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 7) AND option_number = 4;
UPDATE survey_question_options SET option_text = '일주일에 여러번' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 7) AND option_number = 5;

-- 8번 문항: 직장·학교·동네 지인 교류 (업무 제외) 주기는 어떻게 되나요?
UPDATE survey_questions
SET question_text = '직장·학교·동네 지인 교류 (업무 제외) 주기는 어떻게 되나요?'
WHERE question_number = 8;

UPDATE survey_question_options SET option_text = '전혀, 거의 없다' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 8) AND option_number = 1;
UPDATE survey_question_options SET option_text = '몇개월에 한번' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 8) AND option_number = 2;
UPDATE survey_question_options SET option_text = '한달에 한번' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 8) AND option_number = 3;
UPDATE survey_question_options SET option_text = '일주일에 한번' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 8) AND option_number = 4;
UPDATE survey_question_options SET option_text = '일주일에 여러번' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 8) AND option_number = 5;

-- 9번 문항: 위의 1~8번에서 응답한 본인의 상태의 지속 기간이 어떻게 되나요?
UPDATE survey_questions
SET question_text = '위의 1~8번에서 응답한 본인의 상태의 지속 기간이 어떻게 되나요?'
WHERE question_number = 9;

UPDATE survey_question_options SET option_text = '3개월 미만' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 9) AND option_number = 1;
UPDATE survey_question_options SET option_text = '3개월 이상 ~ 6개월 미만' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 9) AND option_number = 2;
UPDATE survey_question_options SET option_text = '6개월 이상 ~ 1년 미만' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 9) AND option_number = 3;
UPDATE survey_question_options SET option_text = '1년 이상 ~ 3년 미만' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 9) AND option_number = 4;
UPDATE survey_question_options SET option_text = '3년 이상' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 9) AND option_number = 5;

-- 10번 문항: 여러분은 평소에 얼마나 자주 외출하시나요?
UPDATE survey_questions
SET question_text = '여러분은 평소에 얼마나 자주 외출하시나요?'
WHERE question_number = 10;

UPDATE survey_question_options SET option_text = '직장이나 학교로 평일은 자주 외출한다' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 10) AND option_number = 1;
UPDATE survey_question_options SET option_text = '여가생활을 위해 자주 외출한다' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 10) AND option_number = 2;
UPDATE survey_question_options SET option_text = '사람을 만나기 위해 가끔 외출한다' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 10) AND option_number = 3;
UPDATE survey_question_options SET option_text = '보통은 집에 있지만, 자신의 취미생활만을 위해 외출한다' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 10) AND option_number = 4;
UPDATE survey_question_options SET option_text = '보통은 집에 있지만, 인근 편의점 등에 외출한다' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 10) AND option_number = 5;
UPDATE survey_question_options SET option_text = '본인 방에서 나오지만, 집 밖으로는 나가지 않는다' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 10) AND option_number = 6;
UPDATE survey_question_options SET option_text = '본인 방에서 거의 나오지 않는다' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 10) AND option_number = 7;

-- 11번 문항: 위 10번 응답에 대한 생활을 한 지 얼마나 되셨나요?
UPDATE survey_questions
SET question_text = '위 10번 응답에 대한 생활을 한 지 얼마나 되셨나요?'
WHERE question_number = 11;

UPDATE survey_question_options SET option_text = '3개월 미만' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 11) AND option_number = 1;
UPDATE survey_question_options SET option_text = '3개월 이상 ~ 6개월 미만' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 11) AND option_number = 2;
UPDATE survey_question_options SET option_text = '6개월 이상 ~ 1년 미만' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 11) AND option_number = 3;
UPDATE survey_question_options SET option_text = '1년 이상 ~ 3년 미만' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 11) AND option_number = 4;
UPDATE survey_question_options SET option_text = '3년 이상' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 11) AND option_number = 5;

-- 12번 문항: 최근 일주일 간 돈을 벌기 위한 경제활동을 하셨나요? 시간은 상관없습니다.
UPDATE survey_questions
SET question_text = '최근 일주일 간 돈을 벌기 위한 경제활동을 하셨나요? 시간은 상관없습니다.'
WHERE question_number = 12;

UPDATE survey_question_options SET option_text = '일을 하였다' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 12) AND option_number = 1;
UPDATE survey_question_options SET option_text = '휴가 및 일시 휴직 중이다' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 12) AND option_number = 2;
UPDATE survey_question_options SET option_text = '일을 하지 않았다' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 12) AND option_number = 3;

-- 13번 문항: 만약 경제활동을 하지 않았다면 지난 한달간 구직 활동을 하셨나요?
UPDATE survey_questions
SET question_text = '만약 경제활동을 하지 않았다면 지난 한달간 구직 활동을 하셨나요?'
WHERE question_number = 13;

UPDATE survey_question_options SET option_text = '구직 활동을 하였다' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 13) AND option_number = 1;
UPDATE survey_question_options SET option_text = '구직 활동을 하지 않았다' WHERE question_id = (SELECT id FROM survey_questions WHERE question_number = 13) AND option_number = 2;