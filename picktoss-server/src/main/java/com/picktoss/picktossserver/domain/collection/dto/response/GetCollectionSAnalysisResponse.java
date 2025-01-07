package com.picktoss.picktossserver.domain.collection.dto.response;

import com.picktoss.picktossserver.global.enums.collection.CollectionCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class GetCollectionSAnalysisResponse {
    @Schema(
            description = "컬렉션 분야와 해당 컬렉션을 푼 횟수 map",
            example = "{\"IT\": 1, \"RAW\": 1}"
    )
    private Map<CollectionCategory, Integer> collectionsAnalysis;
}
