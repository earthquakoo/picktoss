package com.picktoss.picktossserver.domain.event.entity;

import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

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

    public static Event createEvent(int point, int maxContinuousSolvedQuizDateCount, int continuousSolvedQuizDateCount, Member member, LocalDateTime updatedAt) {
        return Event.builder()
                .point(point)
                .continuousSolvedQuizDateCount(continuousSolvedQuizDateCount)
                .maxContinuousSolvedQuizDateCount(maxContinuousSolvedQuizDateCount)
                .member(member)
                .updatedAt(updatedAt)
                .build();
    }

    // Business Logics

    public void addPoint(int point) {
        this.point += point;
    }

    public void usePoint(int point) {
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

    public void updateUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
