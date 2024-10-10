package com.picktoss.picktossserver.domain.quiz.entity;

import com.picktoss.picktossserver.global.baseentity.AuditBase;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "quiz_set_quiz")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class QuizSetQuiz extends AuditBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "elapsed_time_ms")
    private Integer elapsedTimeMs;

    @Column(name = "is_answer")
    private Boolean isAnswer;

    @Column(name = "chose_answer")
    private String choseAnswer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_set_id", nullable = false)
    private QuizSet quizSet;

    // Constructor methods
    public static QuizSetQuiz createQuizSetQuiz(Quiz quiz, QuizSet quizSet) {
        return QuizSetQuiz.builder()
                .quiz(quiz)
                .quizSet(quizSet)
                .build();
    }

    public void updateIsAnswer(Boolean isAnswer) {
        this.isAnswer = isAnswer;
    }

    public void updateChoseAnswer(String choseAnswer) {
        this.choseAnswer = choseAnswer;
    }

    public void updateElapsedTime(int elapsedTimeMs) {
        this.elapsedTimeMs = elapsedTimeMs;
    }
}
