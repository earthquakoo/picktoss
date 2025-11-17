package com.picktoss.picktossserver.domain.quiz.entity;

import com.picktoss.picktossserver.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "daily_quiz_record")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DailyQuizRecord {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "is_daily_quiz_complete", nullable = false)
    private Boolean isDailyQuizComplete;

    @Column(name = "solved_date", nullable = false)
    private LocalDateTime solvedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "dailyQuizRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailyQuizRecordDetail> dailyQuizRecordDetails = new ArrayList<>();

    public static DailyQuizRecord createDailyQuizRecord(Member member) {
        return DailyQuizRecord.builder()
                .isDailyQuizComplete(false)
                .solvedDate(LocalDateTime.now())
                .member(member)
                .dailyQuizRecordDetails(new ArrayList<>())
                .build();

    }

    public void addDailyQuizRecordDetail(DailyQuizRecordDetail dailyQuizRecordDetail) {
        this.dailyQuizRecordDetails.add(dailyQuizRecordDetail);
        dailyQuizRecordDetail.setDailyQuizRecord(this);
    }


    public void updateIsDailyQuizCompleteTrue() {
        this.isDailyQuizComplete = true;
    }
}
