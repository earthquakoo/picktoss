package com.picktoss.picktossserver.domain.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GetAllQuizRecordsResponse {

    private List<GetAllQuizRecordsDto> quizRecords;

    @Getter
    @Builder
    public static class GetAllQuizRecordsDto {
        private LocalDate solvedDate;
        private List<GetAllQuizRecordQuizSetDto> quizSets;
        private List<GetAllQuizRecordDailyQuizDto> dailyQuizRecords;
    }

    @Getter
    @Builder
    public static class GetAllQuizRecordQuizSetDto {
        private Long quizSetId;
        private String quizSetName;
        private String emoji;
        private int totalQuizCount;
        private int correctAnswerCount;
        private LocalDateTime solvedDateTime;
    }

    @Getter
    @Builder
    public static class GetAllQuizRecordDailyQuizDto {
        private Long dailyQuizRecordId;
        private int totalQuizCount;
        private LocalDateTime solvedDateTime;
    }
}
