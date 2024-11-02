package com.picktoss.picktossserver.domain.member.controller.request;

import com.picktoss.picktossserver.global.enums.collection.CollectionField;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
public class UpdateInterestCollectionFieldsRequest {
    @ArraySchema(schema = @Schema(implementation = CollectionField.class, example = "IT, ECONOMY, HISTORY, LANGUAGE, MATH, ETC, ART, MEDICINE, SCIENCE"))
    private List<String> interestCollectionFields;
}
