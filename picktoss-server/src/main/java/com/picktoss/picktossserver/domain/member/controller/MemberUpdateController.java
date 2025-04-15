package com.picktoss.picktossserver.domain.member.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExample;
import com.picktoss.picktossserver.domain.member.dto.request.UpdateMemberCategoryRequest;
import com.picktoss.picktossserver.domain.member.dto.request.UpdateMemberNameRequest;
import com.picktoss.picktossserver.domain.member.dto.request.UpdateQuizNotificationRequest;
import com.picktoss.picktossserver.domain.member.service.MemberUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.MEMBER_NOT_FOUND;

@Tag(name = "Member")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class MemberUpdateController {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberUpdateService memberUpdateService;

    @Operation(summary = "사용자 이름 변경")
    @PatchMapping("/members/update-name")
    @ApiErrorCodeExample(MEMBER_NOT_FOUND)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMemberName(@Valid @RequestBody UpdateMemberNameRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        memberUpdateService.updateMemberName(memberId, request.getName());
    }

    @Operation(summary = "사용자 퀴즈 알림 ON/OFF")
    @PatchMapping("/members/update-quiz-notification")
    @ApiErrorCodeExample(MEMBER_NOT_FOUND)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateQuizNotification(@Valid @RequestBody UpdateQuizNotificationRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        memberUpdateService.updateQuizNotification(memberId, request.isQuizNotificationEnabled());
    }

    @Operation(summary = "사용자 관심 카테고리 변경")
    @PatchMapping("/members/update-category")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMemberCategory(
            @Valid @RequestBody UpdateMemberCategoryRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        memberUpdateService.updateMemberCategory(memberId, request.getCategoryId());
    }
}
