package com.picktoss.picktossserver.domain.quiz.entity;

import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "random_quiz_record")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RandomQuizRecord extends AuditBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "solved_quiz_count", nullable = false)
    private Integer solvedQuizCount;

    @Column(name = "incorrect_quiz_count")
    private Integer incorrectQuizCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public static RandomQuizRecord createRandomQuizRecord(Member member) {
        return RandomQuizRecord.builder()
                .solvedQuizCount(0)
                .incorrectQuizCount(0)
                .member(member)
                .build();
    }

    public void updateQuizCountByCorrectAnswer() {
        this.solvedQuizCount += 1;
    }

    public void updateQuizCountByIncorrectAnswer() {
        this.solvedQuizCount += 1;
        this.incorrectQuizCount += 1;
    }
}
