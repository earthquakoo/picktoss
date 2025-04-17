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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "solved", nullable = false)
    private boolean solved;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "quizSet", orphanRemoval = true)
    private List<QuizSetQuiz> quizSetQuizzes = new ArrayList<>();

    // Constructor methods
    public static QuizSet createQuizSet(String name, Member member) {
        return QuizSet.builder()
                .name(name)
                .solved(false)
                .member(member)
                .build();
    }

    public void updateSolvedBySolvedQuizSet() {
        this.solved = true;
    }
}
