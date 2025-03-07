package com.picktoss.picktossserver.domain.collection.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UpdateCollectionQuizResultResponse {
    private Integer reward;
    private Integer currentConsecutiveTodayQuizDate;
}
