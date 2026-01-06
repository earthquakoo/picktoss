package com.picktoss.picktossserver.domain.fcm.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.fcm.dto.request.SaveFcmTokenRequest;
import com.picktoss.picktossserver.domain.fcm.service.FcmTokenCreateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Fcm")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class FcmController {

    private final JwtTokenProvider jwtTokenProvider;
    private final FcmTokenCreateService fcmTokenCreateService;

    @Operation(summary = "Fcm token 저장")
    @PostMapping("/tokens")
    @ResponseStatus(HttpStatus.OK)
    public void createFcmToken(@Valid @RequestBody SaveFcmTokenRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        fcmTokenCreateService.createFcmToken(memberId, request.getFcmToken());
    }
}
