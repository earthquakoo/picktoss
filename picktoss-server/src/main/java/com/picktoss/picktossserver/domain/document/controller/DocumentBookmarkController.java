package com.picktoss.picktossserver.domain.document.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExamples;
import com.picktoss.picktossserver.domain.document.service.DocumentBookmarkCreateService;
import com.picktoss.picktossserver.domain.document.service.DocumentBookmarkDeleteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;

@Tag(name = "Document")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class DocumentBookmarkController {

    private final JwtTokenProvider jwtTokenProvider;
    private final DocumentBookmarkCreateService documentBookmarkCreateService;
    private final DocumentBookmarkDeleteService documentBookmarkDeleteService;

    @Operation(summary = "북마크 하기")
    @PostMapping("/documents/{document_id}/bookmark")
    @ApiErrorCodeExamples({MEMBER_NOT_FOUND, DOCUMENT_NOT_FOUND})
    @ResponseStatus(HttpStatus.CREATED)
    public void createDocumentBookmark(
            @PathVariable("document_id") Long documentId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        documentBookmarkCreateService.createDocumentBookmark(memberId, documentId);
    }

    @Operation(summary = "북마크 취소")
    @DeleteMapping("/documents/{document_id}/delete")
    @ApiErrorCodeExamples({MEMBER_NOT_FOUND, DOCUMENT_NOT_FOUND, DOCUMENT_BOOKMARK_NOT_FOUND})
    @ResponseStatus(HttpStatus.OK)
    public void deleteDocumentBookmark(
            @PathVariable("document_id") Long documentId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        documentBookmarkDeleteService.deleteDocumentBookmark(memberId, documentId);
    }
}
