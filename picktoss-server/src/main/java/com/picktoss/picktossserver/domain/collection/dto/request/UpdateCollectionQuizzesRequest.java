package com.picktoss.picktossserver.domain.collection.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class UpdateCollectionQuizzesRequest {

    private List<Long> quizzes;
}
