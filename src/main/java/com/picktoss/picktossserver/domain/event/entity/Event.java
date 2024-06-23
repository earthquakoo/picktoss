package com.picktoss.picktossserver.domain.event.entity;

import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static com.picktoss.picktossserver.domain.event.constant.EventConstant.FIVE_DAYS_CONTINUOUS_POINT;
import static com.picktoss.picktossserver.domain.event.constant.EventConstant.ONE_DAYS_POINT;

@Entity
@Getter
@Table(name = "event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Event{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "point", nullable = false)
    private int point;

    @Column(name = "continuous_solved_quiz_date_count", nullable = false)
    private int continuousSolvedQuizDateCount;

    @Column(name = "max_continuous_solved_quiz_date_count", nullable = false)
    private int maxContinuousSolvedQuizDateCount;

    @Column(name = "updated_at", nullable = false)
//    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public static Event createEvent(int point, Member member, LocalDateTime updatedAt) {
        return Event.builder()
                .point(point)
                .continuousSolvedQuizDateCount(0)
                .maxContinuousSolvedQuizDateCount(0)
                .member(member)
                .updatedAt(updatedAt)
                .build();
    }

    // Business Logics

    public void addPointBySolvingTodayQuizFiveContinuousDays() {
        this.point += FIVE_DAYS_CONTINUOUS_POINT;
    }

    public void addPointBySolvingTodayQuizOneContinuousDays() {
        this.point += ONE_DAYS_POINT;
    }

    public void addOnePointWithIncorrectlyGeneratedQuiz() {
        this.point += 1;
    }

    public void usePointByGenerateQuiz(int point) {
        this.point -= point;
    }

    public void initContinuousSolvedQuizDateCount() {
        this.continuousSolvedQuizDateCount = 0;
    }

    public void addContinuousSolvedQuizDateCount() {
        this.continuousSolvedQuizDateCount += 1;
    }

    public void updateMaxContinuousSolvedQuizDateCount(int continuousSolvedQuizDateCount) {
        this.maxContinuousSolvedQuizDateCount = continuousSolvedQuizDateCount;
    }

    public void changeUpdateAtByCurrentTime() {
        this.updatedAt = LocalDateTime.now();
    }

    // 클라이언트 테스트 전용 API(실제 서비스 사용 X)
    public void changePointForTest(int point) {
        this.point = point;

    }
}
