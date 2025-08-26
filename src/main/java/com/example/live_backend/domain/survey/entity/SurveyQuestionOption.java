package com.example.live_backend.domain.survey.entity;

import com.example.live_backend.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "survey_question_options")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SurveyQuestionOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private SurveyQuestion surveyQuestion;

    @Column(name = "option_number", nullable = false)
    private Integer optionNumber;

    @Column(name = "option_text", nullable = false, length = 200)
    private String optionText;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Builder
    public SurveyQuestionOption(SurveyQuestion surveyQuestion, Integer optionNumber, 
                               String optionText, boolean isActive) {
        this.surveyQuestion = surveyQuestion;
        this.optionNumber = optionNumber;
        this.optionText = optionText;
        this.isActive = isActive;
    }

    protected void setSurveyQuestion(SurveyQuestion surveyQuestion) {
        this.surveyQuestion = surveyQuestion;
    }

    public void updateOption(String optionText, boolean isActive) {
        this.optionText = optionText;
        this.isActive = isActive;
    }
}