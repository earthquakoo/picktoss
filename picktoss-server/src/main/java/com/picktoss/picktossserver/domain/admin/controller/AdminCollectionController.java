package com.picktoss.picktossserver.domain.admin.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExample;
import com.picktoss.picktossserver.domain.admin.dto.request.CreateCollectionForAdminRequest;
import com.picktoss.picktossserver.domain.admin.dto.response.GetCollectionsForAdminResponse;
import com.picktoss.picktossserver.domain.admin.service.AdminCollectionSearchService;
import com.picktoss.picktossserver.domain.admin.service.AdminCollectionUpdateService;
import com.picktoss.picktossserver.global.enums.collection.CollectionCategory;
import com.picktoss.picktossserver.global.enums.member.MemberRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.COLLECTION_NOT_FOUND;

@Tag(name = "Admin - Collections")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin")
public class AdminCollectionController {

    private final JwtTokenProvider jwtTokenProvider;
    private final AdminCollectionSearchService adminCollectionSearchService;
    private final AdminCollectionUpdateService adminCollectionUpdateService;

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

        GetCollectionsForAdminResponse response = adminCollectionSearchService.findCollections(collectionCategory, isDeleted, memberRole, quizCount);
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

        GetCollectionsForAdminResponse response = adminCollectionSearchService.searchCollections(keyword, memberName);
        return ResponseEntity.ok().body(response);
    }

    /**
     * PATCH
     */

    @Operation(summary = "컬렉션 공개여부 수정")
    @PatchMapping("/collections/{collection_id}/info/update")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiErrorCodeExample(COLLECTION_NOT_FOUND)
    public void modifyCollectionInfo(
            @Valid @RequestBody CreateCollectionForAdminRequest request,
            @PathVariable(name = "collection_id") Long collectionId
    ) {
        JwtUserInfo adminInfo = jwtTokenProvider.getCurrentUserInfo();
        Long adminId = adminInfo.getMemberId();

        adminCollectionUpdateService.updateCollectionVisibility(collectionId, request.getIsPublic());
    }
}
