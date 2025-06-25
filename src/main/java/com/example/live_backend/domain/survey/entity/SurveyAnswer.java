package com.example.live_backend.domain.survey.entity;

import com.example.live_backend.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "survey_answers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SurveyAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_response_id", nullable = false)
    private SurveyResponse surveyResponse;

    @Column(name = "question_number", nullable = false)
    private Integer questionNumber;

    @Column(name = "answer_number", nullable = false)
    private Integer answerNumber;

    @Builder
    public SurveyAnswer(Integer questionNumber, Integer answerNumber) {
        this.questionNumber = questionNumber;
        this.answerNumber = answerNumber;
    }

    protected void setSurveyResponse(SurveyResponse surveyResponse) {
        this.surveyResponse = surveyResponse;
    }
} 