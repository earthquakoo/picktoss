package com.picktoss.picktossserver.domain.member.controller.request;

import com.picktoss.picktossserver.global.enums.collection.CollectionCategory;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
public class UpdateInterestCollectionCategoriesRequest {
    @ArraySchema(schema = @Schema(implementation = CollectionCategory.class, example = "IT, LAW, BUSINESS_ECONOMY, SOCIETY_POLITICS, LANGUAGE, MEDICINE_PHARMACY, ART, SCIENCE_ENGINEERING, HISTORY_PHILOSOPHY, OTHER"))
    private List<String> interestCollectionCategories;
}
