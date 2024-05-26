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

    @Column(name = "point")
    private int point;

    @Column(name = "continuous_solved_quiz_date_count")
    private int continuousSolvedQuizDateCount;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public static Event createEvent(int point, int continuousAttendanceDatesCount, Member member) {
        return Event.builder()
                .point(point)
                .continuousSolvedQuizDateCount(continuousAttendanceDatesCount)
                .member(member)
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
}
