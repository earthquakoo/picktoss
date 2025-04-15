package com.picktoss.picktossserver.domain.document.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExample;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExamples;
import com.picktoss.picktossserver.domain.document.dto.response.GetAllDocumentsResponse;
import com.picktoss.picktossserver.domain.document.dto.response.GetBookmarkedDocumentsResponse;
import com.picktoss.picktossserver.domain.document.dto.response.GetIsNotPublicDocumentsResponse;
import com.picktoss.picktossserver.domain.document.dto.response.GetSingleDocumentResponse;
import com.picktoss.picktossserver.domain.document.service.DocumentReadService;
import com.picktoss.picktossserver.global.enums.document.BookmarkedDocumentSortOption;
import com.picktoss.picktossserver.global.enums.document.DocumentSortOption;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.AMAZON_SERVICE_EXCEPTION;
import static com.picktoss.picktossserver.core.exception.ErrorInfo.DOCUMENT_NOT_FOUND;

@Tag(name = "Document")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class DocumentReadController {

    private final JwtTokenProvider jwtTokenProvider;
    private final DocumentReadService documentReadService;

    @Operation(summary = "단일 문서 가져오기")
    @GetMapping("/documents/{document_id}")
    @ApiErrorCodeExamples({DOCUMENT_NOT_FOUND, AMAZON_SERVICE_EXCEPTION})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetSingleDocumentResponse> getSingleDocument(
            @PathVariable(name = "document_id") Long documentId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetSingleDocumentResponse documents = documentReadService.findSingleDocument(memberId, documentId);
        return ResponseEntity.ok().body(documents);
    }

    @Operation(summary = "모든 문서 가져오기")
    @GetMapping("/documents")
    @ApiErrorCodeExample(AMAZON_SERVICE_EXCEPTION)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetAllDocumentsResponse> getAllDocuments(
            @RequestParam(defaultValue = "CREATE_AT", value = "sort-option") DocumentSortOption documentSortOption) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetAllDocumentsResponse response = documentReadService.findAllDocuments(memberId, documentSortOption);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "북마크된 모든 문서 가져오기")
    @GetMapping("/documents/bookmarked")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetBookmarkedDocumentsResponse> getBookmarkedDocuments(
            @RequestParam(defaultValue = "CREATE_AT", value = "sort-option") BookmarkedDocumentSortOption documentSortOption
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetBookmarkedDocumentsResponse response = documentReadService.findBookmarkedDocuments(memberId, documentSortOption);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "사용자의 비공개된 문서 수")
    @GetMapping("/documents/not-public")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetIsNotPublicDocumentsResponse> getIsNotPublicDocuments() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetIsNotPublicDocumentsResponse response = documentReadService.findIsNotPublicDocuments(memberId);
        return ResponseEntity.ok().body(response);
    }
}
