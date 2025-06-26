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
@Table(name = "survey_responses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SurveyResponse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @OneToMany(mappedBy = "surveyResponse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyAnswer> answers = new ArrayList<>();

    @Builder
    public SurveyResponse(Long userId) {
        this.userId = userId;
    }

    public void addAnswer(SurveyAnswer answer) {
        this.answers.add(answer);
        answer.setSurveyResponse(this);
    }
} 