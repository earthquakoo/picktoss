package com.picktoss.picktossserver.domain.quiz.entity;

import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import com.picktoss.picktossserver.global.enums.quiz.QuizSetType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "quiz_set")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class QuizSet extends AuditBase {

    @Id @Column(name = "id", length = 300)
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "solved", nullable = false)
    private boolean solved;

    @Enumerated(EnumType.STRING)
    @Column(name = "quiz_set_type")
    private QuizSetType quizSetType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "quizSet", orphanRemoval = true)
    private List<QuizSetQuiz> quizSetQuizzes = new ArrayList<>();

    // Constructor methods
    public static QuizSet createQuizSet(String id, String name, QuizSetType quizSetType, Member member) {
        return QuizSet.builder()
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
