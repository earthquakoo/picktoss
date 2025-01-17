package com.picktoss.picktossserver.domain.admin.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExample;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExamples;
import com.picktoss.picktossserver.domain.admin.controller.request.CreateCollectionForAdminRequest;
import com.picktoss.picktossserver.domain.admin.controller.request.CreateNotificationRequest;
import com.picktoss.picktossserver.domain.admin.controller.request.DeleteNotificationRequest;
import com.picktoss.picktossserver.domain.admin.controller.response.GetCollectionsForAdminResponse;
import com.picktoss.picktossserver.domain.admin.dto.request.AdminLoginRequest;
import com.picktoss.picktossserver.domain.admin.dto.request.SignUpRequest;
import com.picktoss.picktossserver.domain.admin.dto.response.AdminLoginResponse;
import com.picktoss.picktossserver.domain.admin.service.AdminCollectionService;
import com.picktoss.picktossserver.domain.admin.service.AdminCreateService;
import com.picktoss.picktossserver.domain.admin.service.AdminLoginService;
import com.picktoss.picktossserver.domain.admin.service.AdminNotificationService;
import com.picktoss.picktossserver.global.enums.collection.CollectionCategory;
import com.picktoss.picktossserver.global.enums.member.MemberRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;

@Tag(name = "Admin")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin")
public class AdminController {

    private final JwtTokenProvider jwtTokenProvider;
    private final AdminCollectionService adminCollectionService;
    private final AdminCreateService adminCreateService;
    private final AdminLoginService adminLoginService;
    private final AdminNotificationService adminNotificationService;

    /**
     * GET
     */

    @Operation(summary = "컬렉션 관리")
    @GetMapping("/collections")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetCollectionsForAdminResponse> getCollections(
            @RequestParam(required = false, value = "collection-category") CollectionCategory collectionCategory,
            @RequestParam(required = false, value = "is-deleted") Boolean isDeleted,
            @RequestParam(required = false, value = "member-role") MemberRole memberRole,
            @RequestParam(required = false, value = "complaint-count") Integer quizCount
    ) {
        JwtUserInfo adminInfo = jwtTokenProvider.getCurrentUserInfo();
        Long adminId = adminInfo.getMemberId();

        GetCollectionsForAdminResponse response = adminCollectionService.findCollections(collectionCategory, isDeleted, memberRole, quizCount);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "컬렉션 검색하기")
    @GetMapping("/collections/search")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetCollectionsForAdminResponse> searchCollections(
            @RequestParam(required = false, value = "keyword") String keyword,
            @RequestParam(required = false, value = "member-name") String memberName
    ) {
        JwtUserInfo adminInfo = jwtTokenProvider.getCurrentUserInfo();
        Long adminId = adminInfo.getMemberId();

        GetCollectionsForAdminResponse response = adminCollectionService.searchCollections(keyword, memberName);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "컬렉션 공개여부 수정")
    @PostMapping("/collections/{collection_id}/info/update")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiErrorCodeExample(COLLECTION_NOT_FOUND)
    public void modifyCollectionInfo(
            @Valid @RequestBody CreateCollectionForAdminRequest request,
            @PathVariable(name = "collection_id") Long collectionId
    ) {
        JwtUserInfo adminInfo = jwtTokenProvider.getCurrentUserInfo();
        Long adminId = adminInfo.getMemberId();

        adminCollectionService.updateCollectionVisibility(collectionId, request.getIsPublic());
    }

    /**
     * POST
     */

    @Operation(summary = "운영진 회원가입")
    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public void signUp(@Valid @RequestBody SignUpRequest request) {
        adminCreateService.createAdmin(request.getName(), request.getPassword());
    }

    @Operation(summary = "운영진 로그인")
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @ApiErrorCodeExamples({MEMBER_NOT_FOUND, INVALID_PASSWORD})
    public ResponseEntity<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        String accessToken = adminLoginService.login(request.getName(), request.getPassword());
        return ResponseEntity.ok().body(new AdminLoginResponse(accessToken));
    }

    @Operation(summary = "푸시 알림 생성")
    @PostMapping("/notifications")
    public void createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        JwtUserInfo adminInfo = jwtTokenProvider.getCurrentUserInfo();
        Long adminId = adminInfo.getMemberId();

        adminNotificationService.createNotification(request.getTitle(), request.getContent(), request.getMemo(), request.getNotificationType(), request.getNotificationTarget(), request.getIsActive(), request.getNotificationTime(), request.getRepeatDays(), adminId);
    }

    /**
     * DELETE
     */

    @Operation(summary = "푸시 알림 삭제")
    @DeleteMapping("/notifications/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotification(@Valid @RequestBody DeleteNotificationRequest request) {

    }
}