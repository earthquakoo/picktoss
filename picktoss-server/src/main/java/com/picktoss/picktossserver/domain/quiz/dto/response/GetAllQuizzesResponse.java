package com.picktoss.picktossserver.domain.quiz.dto.response;

import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class  GetAllQuizzesResponse {

    private List<GetAllQuizzesDto> quizzes;

    @Getter
    @Builder
    public static class GetAllQuizzesDto {
        private Long id;
        private String name;
        private String question;
        private String answer;
        private String explanation;
        private Boolean isBookmarked;
        private List<String> options;
        private QuizType quizType;
        private Long documentId;
    }
}
