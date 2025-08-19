package com.example.live_backend.domain.survey.entity;

import com.example.live_backend.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "survey_answers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@BatchSize(size = 30)
public class SurveyAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_response_id", nullable = false)
    private SurveyResponse surveyResponse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private SurveyQuestion surveyQuestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private SurveyQuestionOption selectedOption;

    @Column(name = "text_answer", columnDefinition = "TEXT")
    private String textAnswer;

    @Column(name = "number_answer")
    private Integer numberAnswer;

    @Builder
    public SurveyAnswer(SurveyQuestion surveyQuestion, SurveyQuestionOption selectedOption,
                        String textAnswer, Integer numberAnswer) {
        this.surveyQuestion = surveyQuestion;
        this.selectedOption = selectedOption;
        this.textAnswer = textAnswer;
        this.numberAnswer = numberAnswer;
    }

    public Integer getQuestionNumber() {
        return surveyQuestion != null ? surveyQuestion.getQuestionNumber() : null;
    }

    public Integer getAnswerNumber() {
        return selectedOption != null ? selectedOption.getOptionNumber() : numberAnswer;
    }

    protected void setSurveyResponse(SurveyResponse surveyResponse) {
        this.surveyResponse = surveyResponse;
    }
} 