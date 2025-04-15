package com.picktoss.picktossserver.domain.star.service;

import com.picktoss.picktossserver.domain.star.dto.response.GetStarUsageHistories;
import com.picktoss.picktossserver.domain.star.entity.StarHistory;
import com.picktoss.picktossserver.domain.star.repository.StarHistoryRepository;
import com.picktoss.picktossserver.domain.star.repository.StarRepository;
import com.picktoss.picktossserver.global.enums.star.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StarService {

    private final StarHistoryRepository starHistoryRepository;

    public GetStarUsageHistories findStarUsageHistories(Long memberId, TransactionType transactionType) {
        List<StarHistory> starHistories;

        if (transactionType == null) {
            starHistories = starHistoryRepository.findAllByMemberIdOrderByCreatedAt(memberId);
        } else {
            starHistories = starHistoryRepository.findAllByMemberIdAndTransactionTypeOrderByCreatedAt(memberId, transactionType);
        }

        List<GetStarUsageHistories.GetStarUsageHistoriesDto> starHistoriesDtos = new ArrayList<>();
        for (StarHistory starHistory : starHistories) {
            GetStarUsageHistories.GetStarUsageHistoriesDto starHistoriesDto = GetStarUsageHistories.GetStarUsageHistoriesDto.builder()
                    .description(starHistory.getDescription())
                    .changeAmount(starHistory.getChangeAmount())
                    .transactionType(starHistory.getTransactionType())
                    .createdAt(starHistory.getCreatedAt())
                    .build();

            starHistoriesDtos.add(starHistoriesDto);
        }

        return new GetStarUsageHistories(starHistoriesDtos);
    }
}
