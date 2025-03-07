package com.picktoss.picktossserver.domain.collection.entity;

import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "collection_quiz")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CollectionQuiz extends AuditBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;

    @OneToMany(mappedBy = "collectionQuiz", orphanRemoval = true)
    private List<CollectionQuizSetCollectionQuiz> collectionQuizSetCollectionQuizzes = new ArrayList<>();

    // Constructor methods
    public static CollectionQuiz createQuizCollection(Quiz quiz, Collection collection) {
        return CollectionQuiz.builder()
                .quiz(quiz)
                .collection(collection)
                .build();
    }
}
