package com.picktoss.picktossserver.domain.collection.entity;

import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "collection_random_quiz_record")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CollectionRandomQuizRecord extends AuditBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "solved_quiz_count", nullable = false)
    private int solvedQuizCount;

    @Column(name = "correct_quiz_count", nullable = false)
    private int correctQuizCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public static CollectionRandomQuizRecord createCollectionRandomQuizRecord(Member member) {
        return CollectionRandomQuizRecord.builder()
                .solvedQuizCount(0)
                .correctQuizCount(0)
                .member(member)
                .build();
    }

    public void updateQuizCountByCorrectAnswer() {
        this.solvedQuizCount += 1;
        this.correctQuizCount += 1;
    }

    public void updateQuizCountByIncorrectAnswer() {
        this.solvedQuizCount += 1;
    }
}
