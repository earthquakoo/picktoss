package com.picktoss.picktossserver.domain.quiz.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GetQuizRecordResponse {

    private List<GetQuizRecordDto> quizRecords;

    @Getter
    @Builder
    public static class GetQuizRecordDto {
        private Long collectionId;
        private String quizSetId;
        private String name;
        private int quizCount;
        private int score;
        private LocalDateTime solvedDate;
        private int continuousQuizDatesCount;
    }
}
