package com.picktoss.picktossserver.domain.member.controller;


import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.member.controller.response.GetMemberInfoResponse;
import com.picktoss.picktossserver.domain.member.facade.MemberFacade;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MemberController {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberFacade memberFacade;

    @GetMapping("/members/info")
    public ResponseEntity<GetMemberInfoResponse> getMemberInfo() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        String memberId = jwtUserInfo.getMemberId();

        GetMemberInfoResponse memberInfo = memberFacade.findMemberInfo(memberId);
        return ResponseEntity.ok().body(memberInfo);
    }
}