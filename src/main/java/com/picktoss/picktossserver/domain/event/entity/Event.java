package com.picktoss.picktossserver.domain.event.entity;

import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Event{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "point")
    private int point;

    @Column(name = "continuous_attendance_dates_count")
    private int continuousAttendanceDatesCount;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public static Event createEvent(int point, int continuousAttendanceDatesCount, Member member) {
        return Event.builder()
                .point(point)
                .continuousAttendanceDatesCount(continuousAttendanceDatesCount)
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

    public void initContinuousAttendanceDatesCount() {
        this.continuousAttendanceDatesCount = 0;
    }
    public void addContinuousAttendanceDatesCount() {
        this.continuousAttendanceDatesCount += 1;
    }
}
