package com.picktoss.picktossserver.domain.notification.controller;

import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExample;
import com.picktoss.picktossserver.domain.notification.dto.GetNotificationsResponse;
import com.picktoss.picktossserver.domain.notification.service.NotificationSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notification")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class NotificationController {

    private final JwtTokenProvider jwtTokenProvider;
    private final NotificationSearchService notificationSearchService;

    @Operation(summary = "모든 알림 가져오기")
    @GetMapping("/notifications")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetNotificationsResponse> getNotifications() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<GetNotificationsResponse.GetNotificationsDto> response = notificationSearchService.findNotifications(memberId);
        return ResponseEntity.ok().body(new GetNotificationsResponse(response));
    }

    @Operation(summary = "알림 확인")
    @GetMapping("/notifications/{notification_key}/check")
    @ApiErrorCodeExample(ErrorInfo.NOTIFICATION_NOT_FOUND)
    @ResponseStatus(HttpStatus.OK)
    public void getNotificationByNotificationKey(
            @PathVariable("notification_key") String notificationKey
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        notificationSearchService.checkNotification(memberId, notificationKey);
    }
}