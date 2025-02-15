package com.picktoss.picktossserver.domain.admin.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.admin.dto.request.CreateNotificationRequest;
import com.picktoss.picktossserver.domain.admin.dto.request.DeleteNotificationRequest;
import com.picktoss.picktossserver.domain.admin.dto.request.UpdateNotificationRequest;
import com.picktoss.picktossserver.domain.admin.dto.response.GetNotificationsForAdminResponse;
import com.picktoss.picktossserver.domain.admin.dto.response.GetSingleNotificationForAdminResponse;
import com.picktoss.picktossserver.domain.admin.service.*;
import com.picktoss.picktossserver.global.enums.notification.NotificationSearchOption;
import com.picktoss.picktossserver.global.enums.notification.NotificationType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - Notification")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin")
public class AdminNotificationController {

    private final JwtTokenProvider jwtTokenProvider;
    private final AdminNotificationCreateService adminNotificationCreateService;
    private final AdminNotificationSearchService adminNotificationSearchService;
    private final AdminNotificationDeleteService adminNotificationDeleteService;
    private final AdminNotificationUpdateService adminNotificationUpdateService;
    private final AdminNotificationTestService adminNotificationTestService;

    /**
     * GET
     */

    /**
     * AdminNotificationSearchService
     */

    @Operation(summary = "푸시 알림 관리")
    @GetMapping("/notifications")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetNotificationsForAdminResponse> getNotifications(
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        JwtUserInfo adminInfo = jwtTokenProvider.getCurrentUserInfo();
        Long adminId = adminInfo.getMemberId();

        GetNotificationsForAdminResponse response = adminNotificationSearchService.findAllNotification(page);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "푸시 알림 검색")
    @GetMapping("/notifications/search")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetNotificationsForAdminResponse> getNotificationsByKeyword(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "notification-search-option") NotificationSearchOption notificationSearchOption,
            @RequestParam(required = false, value = "notification-type") NotificationType notificationType,
            @RequestParam(required = false, value = "is-active") Boolean isActive,
            @RequestParam(required = false, value = "keyword") String keyword
    ) {
        JwtUserInfo adminInfo = jwtTokenProvider.getCurrentUserInfo();
        Long adminId = adminInfo.getMemberId();

        GetNotificationsForAdminResponse response = adminNotificationSearchService.searchNotifications(page, keyword, notificationSearchOption, notificationType, isActive);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "푸시 알림 상세 정보 조회")
    @GetMapping("/notifications/{notification_id}/info")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetSingleNotificationForAdminResponse> findSingleNotification(
            @PathVariable("notification_id") Long notificationId
    ) {
        JwtUserInfo adminInfo = jwtTokenProvider.getCurrentUserInfo();
        Long adminId = adminInfo.getMemberId();

        GetSingleNotificationForAdminResponse response = adminNotificationSearchService.findSingleNotification(notificationId);
        return ResponseEntity.ok().body(response);
    }

    /**
     * POST
     */

    /**
     * AdminNotificationCreateService
     */

    @Operation(summary = "푸시 알림 생성")
    @PostMapping("/notifications")
    public void createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        JwtUserInfo adminInfo = jwtTokenProvider.getCurrentUserInfo();
        Long adminId = adminInfo.getMemberId();

        adminNotificationCreateService.createNotification(request.getTitle(), request.getContent(), request.getMemo(), request.getNotificationType(), request.getNotificationTarget(), request.getIsActive(), request.getNotificationTime(), request.getRepeatDays(), adminId);
    }

    @Operation(summary = "자신에게 푸시 알림 보내기(테스트 용도)")
    @PostMapping("/test/notifications")
    public void createNotificationTest(@Valid @RequestBody CreateNotificationRequest request) {
        JwtUserInfo adminInfo = jwtTokenProvider.getCurrentUserInfo();
        Long adminId = adminInfo.getMemberId();

        adminNotificationTestService.createNotificationTest(request.getTitle(), request.getContent(), request.getMemo(), request.getNotificationType(), request.getNotificationTarget(), request.getIsActive(), request.getNotificationTime(), request.getRepeatDays(), adminId);
    }

    /**
     * PATCH
     */

    /**
     * AdminNotificationUpdateService
     */

    @Operation(summary = "푸시 알림 상세 및 수정")
    @PatchMapping("/notifications/{notification_id}/update")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateNotification(
            @Valid @RequestBody UpdateNotificationRequest request,
            @PathVariable(name = "notification_id") Long notificationId
    ) {
        JwtUserInfo adminInfo = jwtTokenProvider.getCurrentUserInfo();
        Long adminId = adminInfo.getMemberId();

        adminNotificationUpdateService.updateNotification(notificationId, request.getTitle(), request.getContent(), request.getMemo(), request.getNotificationType(), request.getNotificationTarget(), request.getIsActive(), request.getNotificationTime(), request.getRepeatDays());
    }

    /**
     * DELETE
     */

    /**
     * AdminNotificationDeleteService
     */

    @Operation(summary = "푸시 알림 삭제")
    @DeleteMapping("/notifications/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotification(@Valid @RequestBody DeleteNotificationRequest request) {
        adminNotificationDeleteService.deleteNotifications(request.getNotificationIds());
    }
}
