package com.picktoss.picktossserver.domain.collection.controller.request;

import com.picktoss.picktossserver.global.enums.CollectionDomain;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CreateCollectionRequest {

    private String name;
    private String tag;
    private String emoji;
    private String description;
    private CollectionDomain collectionDomain;
    private List<Long> quizzes;
}
