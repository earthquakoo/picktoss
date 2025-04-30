package com.picktoss.picktossserver.domain.document.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExample;
import com.picktoss.picktossserver.domain.document.dto.response.GetPublicSingleDocumentResponse;
import com.picktoss.picktossserver.domain.document.service.PublicDocumentReadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @ApiErrorCodeExample(DOCUMENT_NOT_FOUND)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetPublicSingleDocumentResponse> getIsPublicSingleDocument(
            @PathVariable("document_id") Long documentId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetPublicSingleDocumentResponse response = publicDocumentReadService.findIsPublicSingleDocument(documentId, memberId);
        return ResponseEntity.ok().body(response);
    }
}
