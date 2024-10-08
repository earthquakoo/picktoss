package com.picktoss.picktossserver.domain.collection.controller.request;

import com.picktoss.picktossserver.global.enums.CollectionDomain;
import com.picktoss.picktossserver.global.enums.CollectionSortOption;
import com.picktoss.picktossserver.global.enums.QuizType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetAllCollectionRequest {

    private CollectionSortOption collectionSortOption;
    private List<CollectionDomain> collectionDomains;
    private QuizType quizType;
    private Integer quizCount;
}
