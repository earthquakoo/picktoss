package com.picktoss.picktossserver.domain.admin.controller.request;

import com.picktoss.picktossserver.global.enums.collection.CollectionCategory;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.Getter;

import java.util.List;

@Getter
public class CreateCollectionForAdminRequest {

    private String collectionName;
    private String collectionDescription;
    private CollectionCategory collectionCategory;
    private List<CreateCollectionQuizzesDto> quizzes;

    @Getter
    public static class CreateCollectionQuizzesDto{
        private String question;
        private String answer;
        private QuizType quizType;
        private List<String> options;
    }

}
