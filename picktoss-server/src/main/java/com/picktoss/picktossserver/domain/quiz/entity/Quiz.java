package com.picktoss.picktossserver.domain.quiz.entity;

import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name = "quiz")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Quiz extends AuditBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "question", columnDefinition = "TEXT", nullable = false)
    private String question;

    @Column(name = "answer", columnDefinition = "TEXT", nullable = false)
    private String answer;

    @Column(name = "explanation", columnDefinition = "TEXT", nullable = false)
    private String explanation;

    @Column(name = "delivered_count", nullable = false)
    private int deliveredCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "quiz_type", nullable = false)
    private QuizType quizType;

    @Column(name = "correct_answer_count", nullable = false)
    private int correctAnswerCount;

    @Column(name = "is_review_needed", nullable = false)
    private boolean isReviewNeeded;

    @Column(name = "is_latest", nullable = false)
    private Boolean isLatest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Option> options = new HashSet<>();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<QuizSetQuiz> quizSetQuizzes = new HashSet<>();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DailyQuizRecordDetail> dailyQuizRecordDetails = new HashSet<>();

    // Business Logics
    public void addCorrectAnswerCount() {
        this.correctAnswerCount += 1;
    }

    public void addDeliveredCount() {
        this.deliveredCount += 1;
    }

    public void updateIsReviewNeededTrueByWrongAnswer() {
        this.isReviewNeeded = true;
    }

    public void updateIsReviewNeededFalseByCorrectAnswer() {
        this.isReviewNeeded = false;
    }

    public void updateIsReviewNeededFalseByWrongAnswerConfirm()  {
        this.isReviewNeeded = false;
    }

    public void updateQuizInfoByInvalidQuiz(String question, String answer, String explanation) {
        if (question != null && !question.isEmpty()) {
            this.question = question;
        }

        if (answer != null && !answer.isEmpty()) {
            this.answer = answer;
        }

        if (explanation != null && !explanation.isEmpty()) {
            this.explanation = explanation;
        }
    }
}
