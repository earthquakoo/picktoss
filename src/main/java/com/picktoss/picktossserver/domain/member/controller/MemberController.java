package com.picktoss.picktossserver.domain.member.controller;


import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.member.controller.response.CheckContinuousSolvedDatesResponse;
import com.picktoss.picktossserver.domain.member.controller.response.GetMemberInfoResponse;
import com.picktoss.picktossserver.domain.member.facade.MemberFacade;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "5. Member")
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

    @Operation(summary = "Number of continuous quizzes")
    @GetMapping("/continuous-solved-dates")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CheckContinuousSolvedDatesResponse> checkContinuousSolvedDates() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        int checkContinuousQuizDatesCount = memberFacade.checkContinuousQuizDatesCount(memberId);
        return ResponseEntity.ok().body(new CheckContinuousSolvedDatesResponse(checkContinuousQuizDatesCount));
    }
}