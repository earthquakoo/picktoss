package com.picktoss.picktossserver.domain.star.dto.response;

import com.picktoss.picktossserver.global.enums.star.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GetStarUsageHistories {

    private List<GetStarUsageHistoriesDto> starHistories;

    @Getter
    @Builder
    public static class GetStarUsageHistoriesDto {
        private String description;
        private Integer changeAmount;
        private TransactionType transactionType;
        private LocalDateTime createdAt;
    }
}
