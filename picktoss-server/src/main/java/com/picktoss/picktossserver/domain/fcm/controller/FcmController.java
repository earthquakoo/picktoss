package com.picktoss.picktossserver.domain.fcm.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.fcm.dto.request.SaveFcmTokenRequest;
import com.picktoss.picktossserver.domain.fcm.service.FcmCreateService;
import com.picktoss.picktossserver.domain.fcm.service.FcmSendManager;
import com.picktoss.picktossserver.domain.fcm.service.FcmSendService;
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
    private final FcmCreateService fcmCreateService;
    private final FcmSendService fcmSendService;
    private final FcmSendManager fcmSendManager;

    @Operation(summary = "Fcm token 저장")
    @PostMapping("/tokens")
    @ResponseStatus(HttpStatus.OK)
    public void saveFcmToken(@Valid @RequestBody SaveFcmTokenRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        fcmCreateService.saveFcmToken(memberId, request.getFcmToken());
    }

//    @Operation(summary = "앱 알림 푸시")
//    @PostMapping("/message-send")
//    @ResponseStatus(HttpStatus.OK)
//    public void messageSend(@Valid @RequestBody FcmNotificationRequestDto request) {
//        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
//        Long memberId = jwtUserInfo.getMemberId();
//
//        fcmSendService.sendByToken(request.getTitle(), request.getBody(), request.getContent(), memberId);
//    }
//
//    @Operation(summary = "테스트 알림 푸시")
//    @PostMapping("/send")
//    @ResponseStatus(HttpStatus.OK)
//    public int sendMessage(@Valid @RequestBody FcmSendDto request) throws IOException {
//        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
//        Long memberId = jwtUserInfo.getMemberId();
//
//        return fcmSendManager.sendMessageTo(request);
//    }
}
