package com.picktoss.picktossserver.domain.keypoint.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GetKeyPointsSetTodayResponse {
    private String questionSetId;
    private String message;
}
