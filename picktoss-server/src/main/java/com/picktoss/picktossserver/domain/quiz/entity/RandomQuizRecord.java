package com.picktoss.picktossserver.domain.quiz.entity;

import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import com.picktoss.picktossserver.global.utils.StringListConvert;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

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

    @Convert(converter = StringListConvert.class)
    @Column(name = "today_solved_quizzes")
    private List<Long> todaySolvedQuizzes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public static RandomQuizRecord createRandomQuizRecord(List<Long> todaySolvedQuizzes, Member member) {
        return RandomQuizRecord.builder()
                .todaySolvedQuizzes(todaySolvedQuizzes)
                .member(member)
                .build();
    }

    public void updateTodaySolvedQuizzes(List<Long> todaySolvedQuizzes) {
        this.todaySolvedQuizzes.addAll(todaySolvedQuizzes);
        this.todaySolvedQuizzes = this.todaySolvedQuizzes.stream()
                .distinct()
                .collect(Collectors.toList());
    }
}
