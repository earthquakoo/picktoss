package com.picktoss.picktossserver.domain.fcm.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.fcm.dto.request.SaveFcmTokenRequest;
import com.picktoss.picktossserver.domain.fcm.dto.response.FcmTokenStatusResponse;
import com.picktoss.picktossserver.domain.fcm.service.FcmTokenCreateService;
import com.picktoss.picktossserver.domain.fcm.service.FcmTokenReadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Fcm")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class FcmController {

    private final JwtTokenProvider jwtTokenProvider;
    private final FcmTokenCreateService fcmTokenCreateService;
    private final FcmTokenReadService fcmTokenReadService;

    @Operation(summary = "Fcm token 저장")
    @PostMapping("/tokens")
    @ResponseStatus(HttpStatus.OK)
    public void createFcmToken(@Valid @RequestBody SaveFcmTokenRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        fcmTokenCreateService.createFcmToken(memberId, request.getFcmToken());
    }

    @Operation(summary = "FCM 토큰 등록 여부 조회")
    @GetMapping("/tokens")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<FcmTokenStatusResponse> getFcmTokenStatus() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        boolean isRegistered = fcmTokenReadService.isFcmTokenRegistered(memberId);
        return ResponseEntity.ok().body(new FcmTokenStatusResponse(isRegistered));
    }
}
