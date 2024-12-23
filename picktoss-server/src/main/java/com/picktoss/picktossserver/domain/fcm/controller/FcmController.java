package com.picktoss.picktossserver.domain.fcm.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.fcm.controller.request.FcmNotificationRequestDto;
import com.picktoss.picktossserver.domain.fcm.controller.request.SaveFcmTokenRequest;
import com.picktoss.picktossserver.domain.fcm.facade.FcmFacade;
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

    private final FcmFacade fcmFacade;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "Fcm token 저장")
    @PostMapping("/tokens")
    @ResponseStatus(HttpStatus.OK)
    public void saveFcmToken(@Valid @RequestBody SaveFcmTokenRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        fcmFacade.saveFcmToken(memberId, request.getFcmToken());
    }

    @Operation(summary = "앱 알림 푸시")
    @PostMapping("/message-send")
    @ResponseStatus(HttpStatus.OK)
    public void messageSend(@Valid @RequestBody FcmNotificationRequestDto request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        fcmFacade.sendByToken(request.getTitle(), request.getBody(), request.getContent(), memberId);
    }
}
