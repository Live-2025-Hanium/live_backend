package com.example.live_backend.domain.survey.entity;

import com.example.live_backend.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "survey_questions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SurveyQuestion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_number", nullable = false, unique = true)
    private Integer questionNumber;

    @Column(name = "question_text", nullable = false, length = 500)
    private String questionText;


    @Column(name = "is_required", nullable = false)
    private boolean isRequired;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @OneToMany(mappedBy = "surveyQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("optionNumber ASC")
    private List<SurveyQuestionOption> options = new ArrayList<>();

    @Builder
    public SurveyQuestion(Integer questionNumber, String questionText, 
                         boolean isRequired, boolean isActive) {
        this.questionNumber = questionNumber;
        this.questionText = questionText;
        this.isRequired = isRequired;
        this.isActive = isActive;
    }

    public void addOption(SurveyQuestionOption option) {
        this.options.add(option);
        option.setSurveyQuestion(this);
    }

    public void updateQuestion(String questionText, boolean isRequired, boolean isActive) {
        this.questionText = questionText;
        this.isRequired = isRequired;
        this.isActive = isActive;
    }
}