package com.picktoss.picktossserver.domain.collection.entity;

import com.picktoss.picktossserver.global.baseentity.AuditBase;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "collection_quiz_set_collection_quiz")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CollectionQuizSetCollectionQuiz extends AuditBase {

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
    @JoinColumn(name = "collection_quiz_id", nullable = false)
    private CollectionQuiz collectionQuiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_quiz_set_id", nullable = false)
    private CollectionQuizSet collectionQuizSet;

    // Constructor methods
    public static CollectionQuizSetCollectionQuiz createCollectionQuizSetCollectionQuiz(CollectionQuiz collectionQuiz, CollectionQuizSet collectionQuizSet) {
        return CollectionQuizSetCollectionQuiz.builder()
                .collectionQuiz(collectionQuiz)
                .collectionQuizSet(collectionQuizSet)
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
