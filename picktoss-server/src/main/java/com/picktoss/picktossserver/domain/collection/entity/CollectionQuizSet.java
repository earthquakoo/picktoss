package com.picktoss.picktossserver.domain.collection.entity;

import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import com.picktoss.picktossserver.global.enums.quiz.QuizSetType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "collection_quiz_set")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CollectionQuizSet extends AuditBase {

    @Id @Column(name = "id", length = 300)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "solved", nullable = false)
    private boolean solved;

    @Enumerated(EnumType.STRING)
    @Column(name = "quiz_set_type", nullable = false)
    private QuizSetType quizSetType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;

    @OneToMany(mappedBy = "collectionQuizSet", orphanRemoval = true)
    private List<CollectionQuizSetCollectionQuiz> collectionQuizSetCollectionQuizzes = new ArrayList<>();

    // Constructor methods
    public static CollectionQuizSet createCollectionQuizSet(String id, String name, QuizSetType quizSetType, Member member) {
        return CollectionQuizSet.builder()
                .id(id)
                .name(name)
                .solved(false)
                .quizSetType(quizSetType)
                .member(member)
                .build();
    }

    public void updateSolved() {
        this.solved = true;
    }
}
