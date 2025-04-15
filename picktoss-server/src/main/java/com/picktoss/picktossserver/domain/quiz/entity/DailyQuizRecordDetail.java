package com.picktoss.picktossserver.domain.quiz.entity;

import com.picktoss.picktossserver.global.baseentity.AuditBase;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "daily_quiz_record_detail")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DailyQuizRecordDetail extends AuditBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "is_answer", nullable = false)
    private Boolean isAnswer;

    @Column(name = "chose_answer", nullable = false)
    private String choseAnswer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_quiz_record_id", nullable = false)
    private DailyQuizRecord dailyQuizRecord;

    public static DailyQuizRecordDetail createDailyQuizRecordDetail(Boolean isAnswer, String choseAnswer, Quiz quiz, DailyQuizRecord dailyQuizRecord) {
        DailyQuizRecordDetail dailyQuizRecordDetail = DailyQuizRecordDetail.builder()
                .isAnswer(isAnswer)
                .choseAnswer(choseAnswer)
                .quiz(quiz)
                .dailyQuizRecord(dailyQuizRecord)
                .build();

        dailyQuizRecord.addDailyQuizRecordDetail(dailyQuizRecordDetail);
        return dailyQuizRecordDetail;
    }
}
