package com.picktoss.picktossserver.domain.quiz.entity;

import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
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

    @Column(name = "solved", nullable = false)
    private boolean solved;

    @Column(name = "is_today_quiz_set", nullable = false)
    private boolean isTodayQuizSet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "quizSet", orphanRemoval = true)
    private List<QuizSetQuiz> quizSetQuizzes = new ArrayList<>();

    // Constructor methods
    public static QuizSet createQuizSet(String id, boolean isTodayQuizSet, Member member) {
        return QuizSet.builder()
                .id(id)
                .solved(false)
                .isTodayQuizSet(isTodayQuizSet)
                .member(member)
                .build();
    }

    public void updateSolved() {
        this.solved = true;
    }
}
