package com.picktoss.picktossserver.domain.collection.controller.request;

import lombok.Getter;

import java.util.List;

@Getter
public class UpdateCollectionQuizzesRequest {

    private List<Long> quizzes;
}
