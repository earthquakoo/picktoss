package com.picktoss.picktossserver.domain.quiz.entity;

import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.question.entity.QuestionQuestionSet;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import com.picktoss.picktossserver.global.enums.QuizType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "options")
    private List<String> options;

    @Column(name = "delivered_count", nullable = false)
    private int deliveredCount;

    @Column(name = "quiz_type", nullable = false)
    private QuizType quizType;

    @Column(name = "bookmark", nullable = false)
    private boolean bookmark;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizSetQuiz> quizSetQuizzes = new ArrayList<>();

    // Business Logics
    public void updateBookmark(boolean bookmark) {
        this.bookmark = bookmark;
    }
}
