package com.picktoss.picktossserver.domain.quiz.entity;

import com.picktoss.picktossserver.global.baseentity.AuditBase;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

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

    @Column(name = "elapsed_time")
    private LocalTime elapsedTime;

    @Column(name = "is_answer")
    private Boolean isAnswer;

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

    public void updateElapsedTime(LocalTime elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
}
