package com.picktoss.picktossserver.domain.publicquizcollection.dto.request;

import com.picktoss.picktossserver.global.enums.document.PublicQuizCollectionCategory;
import lombok.Getter;

@Getter
public class CreatePublicQuizCollectionRequest {
    private String explanation;
    private PublicQuizCollectionCategory publicQuizCollectionCategory;
}
