package com.picktoss.picktossserver.domain.member.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.member.dto.request.DeleteMemberRequest;
import com.picktoss.picktossserver.domain.member.service.MemberDeleteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class MemberDeleteController {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberDeleteService memberDeleteService;

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/members/withdrawal")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMember(@Valid @RequestBody DeleteMemberRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        memberDeleteService.deleteMember(memberId, request.getDetail(), request.getReason());
    }
}
