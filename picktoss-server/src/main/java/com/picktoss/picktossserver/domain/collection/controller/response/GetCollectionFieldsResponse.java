package com.picktoss.picktossserver.domain.collection.controller.response;

import com.picktoss.picktossserver.global.enums.collection.CollectionField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
public class GetCollectionFieldsResponse {

    private Set<CollectionField> collectionFields;
}
