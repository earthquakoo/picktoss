package com.picktoss.picktossserver.domain.quiz.entity;

import com.picktoss.picktossserver.domain.collection.entity.CollectionQuiz;
import com.picktoss.picktossserver.domain.document.entity.Document;
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

    @Column(name = "delivered_count", nullable = false)
    private int deliveredCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "quiz_type", nullable = false)
    private QuizType quizType;

    @Column(name = "bookmark", nullable = false)
    private boolean bookmark;

    @Column(name = "incorrect_answer_count", nullable = false)
    private int incorrectAnswerCount;

    @Column(name = "latest", nullable = false)
    private boolean latest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Option> options = new ArrayList<>();

    @OneToMany(mappedBy = "quiz", orphanRemoval = true)
    private List<QuizSetQuiz> quizSetQuizzes = new ArrayList<>();

    @OneToMany(mappedBy = "quiz", orphanRemoval = true)
    private List<CollectionQuiz> collectionQuizzes = new ArrayList<>();

    // Business Logics
    public void updateBookmark(boolean bookmark) {
        this.bookmark = bookmark;
    }

    public void updateQuizLatestByDocumentReUpload() {
        this.latest = false;
    }

    public void addIncorrectAnswerCount() {
        this.incorrectAnswerCount += 1;
    }

    public void addDeliveredCount() {
        this.deliveredCount += 1;
    }
}
