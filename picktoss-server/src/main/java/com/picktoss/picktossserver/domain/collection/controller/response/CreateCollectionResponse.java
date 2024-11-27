package com.picktoss.picktossserver.domain.collection.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreateCollectionResponse {
    private Long collectionId;
}
