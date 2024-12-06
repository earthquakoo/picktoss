package com.picktoss.picktossserver.domain.collection.controller.request;

import com.picktoss.picktossserver.global.enums.collection.CollectionCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateCollectionInfoRequest {

    @Schema(example = "컬렉션 제목")
    private String name;

    @Schema(example = "\uD83D\uDCD9")
    private String emoji;

    @Size(max = 200, message = "200자이내로 입력해주세요.")
    @Schema(example = "컬렉션 설명")
    private String description;

    @Schema(implementation = CollectionCategory.class, example = "IT, LAW, BUSINESS_ECONOMY, SOCIETY_POLITICS, LANGUAGE, MEDICINE_PHARMACY, ART, SCIENCE_ENGINEERING, HISTORY_PHILOSOPHY, OTHER")
    private CollectionCategory collectionCategory;
}
