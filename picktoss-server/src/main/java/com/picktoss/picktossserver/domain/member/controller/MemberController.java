package com.picktoss.picktossserver.domain.member.controller;


import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExample;
import com.picktoss.picktossserver.domain.member.dto.request.*;
import com.picktoss.picktossserver.domain.member.dto.response.GetMemberInfoResponse;
import com.picktoss.picktossserver.domain.member.service.MemberDeleteService;
import com.picktoss.picktossserver.domain.member.service.MemberSearchService;
import com.picktoss.picktossserver.domain.member.service.MemberUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.MEMBER_NOT_FOUND;

@Tag(name = "Member")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class MemberController {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberDeleteService memberDeleteService;
    private final MemberSearchService memberSearchService;
    private final MemberUpdateService memberUpdateService;

    /**
     * GET
     */

    @Operation(summary = "Get member info")
    @GetMapping("/members/info")
    @ApiErrorCodeExample(MEMBER_NOT_FOUND)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetMemberInfoResponse> getMemberInfo() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetMemberInfoResponse memberInfo = memberSearchService.findMemberInfo(memberId);
        return ResponseEntity.ok().body(memberInfo);
    }

    @Operation(summary = "초대 링크 보상 확인?")
    @GetMapping("/members/reward")
    @ResponseStatus(HttpStatus.OK)
    public void getInviteLinkMember() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();
    }

    /**
     * PATCH
     */

    @Operation(summary = "사용자 이름 수정")
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

    @Operation(summary = "관심분야 태그 설정")
    @PatchMapping("/members/update-collection-categories")
    @ApiErrorCodeExample(MEMBER_NOT_FOUND)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateInterestCollectionCategories(@Valid @RequestBody UpdateInterestCollectionCategoriesRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        memberUpdateService.updateInterestCollectionCategories(memberId, request.getInterestCollectionCategories());
    }

    @Operation(summary = "오늘의 퀴즈 관리(오늘의 퀴즈 개수 설정)")
    @PatchMapping("/members/update-today-quiz-count")
    @ApiErrorCodeExample(MEMBER_NOT_FOUND)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateTodayQuizCount(@Valid @RequestBody UpdateTodayQuizCountRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        memberUpdateService.updateTodayQuizCount(memberId, request.getTodayQuizCount());
    }

    /**
     * DELETE
     */

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/members/withdrawal")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMember(@Valid @RequestBody DeleteMemberRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        memberDeleteService.deleteMember(memberId, request.getDetail(), request.getReason());
    }
}