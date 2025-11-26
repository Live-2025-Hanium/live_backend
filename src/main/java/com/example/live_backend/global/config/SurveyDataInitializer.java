package com.example.live_backend.global.config;

import com.example.live_backend.domain.survey.entity.SurveyQuestion;
import com.example.live_backend.domain.survey.entity.SurveyQuestionOption;
import com.example.live_backend.domain.survey.repository.SurveyQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SurveyDataInitializer implements ApplicationRunner {

    private final SurveyQuestionRepository surveyQuestionRepository;

    @Override
    @Transactional
    @Profile({"dev", "local"}) // test 환경에서는 실행 안됨
    public void run(ApplicationArguments    args) throws Exception {

        if (surveyQuestionRepository.count() > 0) {
            return;
        }

        initializeSurveyQuestions();
    }

    private void initializeSurveyQuestions() {

        // === 고립 청년 판별 문항 (1~9번) ===

        // 문항 1: 조언을 구할 수 있는 사람
        createQuestionWithOptions(1,
                "중요하거나 어려운 일이 있을 때 주로 누구에게 조언을 구하나요?",
                SurveyQuestion.QuestionType.SINGLE_CHOICE,
                "가족, 친척", "친구", "기타 타인 (이웃, 직장동료 등)", "없음");

        // 문항 2: 급한 일을 부탁할 사람
        createQuestionWithOptions(2,
                "급한 일이 생겼을 때 주로 누구에게 부탁하나요?",
                SurveyQuestion.QuestionType.SINGLE_CHOICE,
                "가족, 친척", "친구", "기타 타인 (이웃, 직장동료 등)", "없음");

        // 문항 3: 돈을 빌릴 사람
        createQuestionWithOptions(3,
                "돈을 빌려야 할 때 주로 누구에게 부탁하나요?",
                SurveyQuestion.QuestionType.SINGLE_CHOICE,
                "가족, 친척", "친구", "기타 타인 (이웃, 직장동료 등)", "없음");

        // 문항 4: 속마음을 털어놓을 사람
        createQuestionWithOptions(4,
                "속마음을 털어놓고 싶을 때 주로 누구에게 얘기하나요?",
                SurveyQuestion.QuestionType.SINGLE_CHOICE,
                "가족, 친척", "친구", "기타 타인 (이웃, 직장동료 등)", "없음");

        // 문항 5: 가족 대면 교류 주기
        createQuestionWithOptions(5,
                "가족들과의 대면 교류 주기는 어떻게 되나요?",
                SurveyQuestion.QuestionType.SINGLE_CHOICE,
                "전혀, 거의 없다", "몇 개월에 한 번", "한 달에 한 번", "일주일에 한 번", "일주일에 여러 번");

        // 문항 6: 친인척 대면 교류 주기
        createQuestionWithOptions(6,
                "친인척들과의 대면 교류 주기는 어떻게 되나요?",
                SurveyQuestion.QuestionType.SINGLE_CHOICE,
                "전혀, 거의 없다", "몇 개월에 한 번", "한 달에 한 번", "일주일에 한 번", "일주일에 여러 번");

        // 문항 7: 친한 친구/지인 교류 주기
        createQuestionWithOptions(7,
                "친한 친구/지인 교류 주기는 어떻게 되나요?",
                SurveyQuestion.QuestionType.SINGLE_CHOICE,
                "전혀, 거의 없다", "몇 개월에 한 번", "한 달에 한 번", "일주일에 한 번", "일주일에 여러 번");

        // 문항 8: 직장·학교·동네 지인 교류 주기
        createQuestionWithOptions(8,
                "직장·학교·동네 지인 교류 (업무 제외) 주기는 어떻게 되나요?",
                SurveyQuestion.QuestionType.SINGLE_CHOICE,
                "전혀, 거의 없다", "몇 개월에 한 번", "한 달에 한 번", "일주일에 한 번", "일주일에 여러 번");

        // 문항 9: 상태 지속 기간
        createQuestionWithOptions(9,
                "위의 1~8번에서 응답한 본인의 상태의 지속 기간이 어떻게 되나요?",
                SurveyQuestion.QuestionType.SINGLE_CHOICE,
                "3개월 미만", "3개월 이상 ~ 6개월 미만", "6개월 이상 ~ 1년 미만", "1년 이상 ~ 3년 미만", "3년 이상");

        // === 은둔 청년 판별 문항 (10~13번) ===

        // 문항 10: 외출 빈도
        createQuestionWithOptions(10,
                "여러분은 평소에 얼마나 자주 외출하시나요?",
                SurveyQuestion.QuestionType.SINGLE_CHOICE,
                "직장이나 학교로 평일은 자주 외출한다",
                "여가생활을 위해 자주 외출한다",
                "사람을 만나기 위해 가끔 외출한다",
                "보통은 집에 있지만, 자신의 취미생활만을 위해 외출한다",
                "보통은 집에 있지만, 인근 편의점 등에 외출한다",
                "본인 방에서 나오지만, 집 밖으로는 나가지 않는다",
                "본인 방에서 거의 나오지 않는다");

        // 문항 11: 해당 생활 지속 기간
        createQuestionWithOptions(11,
                "위 10번 응답에 대한 생활을 한 지 얼마나 되셨나요?",
                SurveyQuestion.QuestionType.SINGLE_CHOICE,
                "3개월 미만", "3개월 이상 ~ 6개월 미만", "6개월 이상 ~ 1년 미만", "1년 이상 ~ 3년 미만", "3년 이상");

        // 문항 12: 경제활동 여부
        createQuestionWithOptions(12,
                "최근 일주일 간 돈을 벌기 위한 경제활동을 하셨나요? 시간은 상관없습니다.",
                SurveyQuestion.QuestionType.SINGLE_CHOICE,
                "일을 하였다", "휴가 및 일시 휴직 중이다", "일을 하지 않았다");

        // 문항 13: 구직 활동 여부
        createQuestionWithOptions(13,
                "만약 경제활동을 하지 않았다면 지난 한 달간 구직 활동을 하셨나요?",
                SurveyQuestion.QuestionType.SINGLE_CHOICE,
                "구직 활동을 하였다", "구직 활동을 하지 않았다");
    }

    private void createQuestionWithOptions(int questionNumber,
                                           String questionText,
                                           SurveyQuestion.QuestionType questionType,
                                           String... optionTexts) {

        SurveyQuestion question = SurveyQuestion.builder()
                .questionNumber(questionNumber)
                .questionText(questionText)
                .questionType(questionType)
                .isRequired(true)
                .isActive(true)
                .build();

        for (int i = 0; i < optionTexts.length; i++) {
            question.addOption(SurveyQuestionOption.builder()
                    .optionNumber(i + 1)
                    .optionText(optionTexts[i])
                    .isActive(true)
                    .build());
        }

        surveyQuestionRepository.save(question);
    }
}
