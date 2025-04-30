package com.picktoss.picktossserver.domain.star.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.star.dto.response.GetStarUsageHistories;
import com.picktoss.picktossserver.domain.star.service.StarService;
import com.picktoss.picktossserver.global.enums.star.TransactionType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Star")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class StarController {

    private final JwtTokenProvider jwtTokenProvider;
    private final StarService starService;

    @Operation(summary = "별 사용 내역")
    @GetMapping("/stars")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetStarUsageHistories> getStarUsageHistories(
            @RequestParam(required = false, value = "usage-history-sort-type") TransactionType transactionType
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetStarUsageHistories response = starService.findStarUsageHistories(memberId, transactionType);
        return ResponseEntity.ok().body(response);
    }
}
