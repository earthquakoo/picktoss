package com.picktoss.picktossserver.domain.quiz.dto.dto;

import com.picktoss.picktossserver.global.enums.quiz.QuizSetType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class SolvedQuizRecordDto {
    private LocalDate solvedDate;
    private String id;
    private String name;
    private int quizCount;
    private int score;
    private QuizSetType quizSettype;
}
