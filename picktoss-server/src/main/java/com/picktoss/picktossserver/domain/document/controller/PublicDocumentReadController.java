package com.picktoss.picktossserver.domain.document.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExamples;
import com.picktoss.picktossserver.domain.document.dto.response.GetPublicSingleDocumentResponse;
import com.picktoss.picktossserver.domain.document.service.PublicDocumentReadService;
import com.picktoss.picktossserver.global.enums.quiz.QuizSortOption;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.CANNOT_VIEW_UNPUBLISHED_DOCUMENT;
import static com.picktoss.picktossserver.core.exception.ErrorInfo.DOCUMENT_NOT_FOUND;

@Tag(name = "Document - Public")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class PublicDocumentReadController {

    private final JwtTokenProvider jwtTokenProvider;
    private final PublicDocumentReadService publicDocumentReadService;

    @Operation(summary = "공개된 문서 정보 조회(+ 상세정보)")
    @GetMapping("/documents/{document_id}/public")
    @ApiErrorCodeExamples({DOCUMENT_NOT_FOUND, CANNOT_VIEW_UNPUBLISHED_DOCUMENT})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetPublicSingleDocumentResponse> getIsPublicSingleDocument(
            @PathVariable("document_id") Long documentId,
            @RequestParam(defaultValue = "CREATED_AT", value = "sort-option") QuizSortOption quizSortOption
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo != null ? jwtUserInfo.getMemberId() : null;

        GetPublicSingleDocumentResponse response = publicDocumentReadService.findIsPublicSingleDocument(documentId, memberId, quizSortOption);
        return ResponseEntity.ok().body(response);
    }
}
