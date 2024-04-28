package com.picktoss.picktossserver.domain.quiz.entity;

import com.picktoss.picktossserver.domain.question.entity.Question;
import com.picktoss.picktossserver.domain.question.entity.QuestionSet;
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

    @Column(name = "answer")
    private boolean answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_set_id", nullable = false)
    private QuizSet quizSet;

    // Business Logics
    public void updateAnswer(boolean answer) {
        this.answer = answer;
    }
}
