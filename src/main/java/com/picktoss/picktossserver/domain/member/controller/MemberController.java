package com.picktoss.picktossserver.domain.member.controller;


import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.member.controller.request.UpdateMemberNameRequest;
import com.picktoss.picktossserver.domain.member.controller.request.UpdateQuizNotificationRequest;
import com.picktoss.picktossserver.domain.member.controller.response.GetMemberInfoResponse;
import com.picktoss.picktossserver.domain.member.facade.MemberFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "6. Member")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MemberController {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberFacade memberFacade;

    @Operation(summary = "Get member info")
    @GetMapping("/members/info")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetMemberInfoResponse> getMemberInfo() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetMemberInfoResponse memberInfo = memberFacade.findMemberInfo(memberId);
        return ResponseEntity.ok().body(memberInfo);
    }

    @Operation(summary = "Update member name")
    @PatchMapping("/members/update-name")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMemberName(@Valid @RequestBody UpdateMemberNameRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        memberFacade.updateMemberName(memberId, request.getName());
    }

    @Operation(summary = "Update quiz notification")
    @PatchMapping("/members/update-quiz-notification")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateQuizNotification(@Valid @RequestBody UpdateQuizNotificationRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        memberFacade.updateQuizNotification(memberId, request.isQuizNotificationEnabled());
    }
}