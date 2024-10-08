package com.picktoss.picktossserver.domain.collection.controller.request;

import com.picktoss.picktossserver.global.enums.CollectionDomain;
import lombok.Getter;

@Getter
public class UpdateCollectionInfoRequest {

    private String name;
    private String tag;
    private String description;
    private String emoji;
    private CollectionDomain collectionDomain;
}
