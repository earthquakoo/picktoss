package com.picktoss.picktossserver.domain.quiz.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GetQuizRecordResponse {

    private int currentConsecutiveDays;
    private int maxConsecutiveDays;
    private List<GetQuizRecordDto> quizRecords;
    private List<GetCollectionRecordDto> collectionRecords;

    @Getter
    @Builder
    public static class GetQuizRecordDto {
        private String quizSetId;
        private String name;
        private int quizCount;
        private int score;
        private LocalDateTime solvedDate;
    }

    @Getter
    @Builder
    public static class GetCollectionRecordDto {
        private Long collectionId;
        private String name;
        private int quizCount;
        private int score;
        private LocalDateTime solvedDate;
    }
}
