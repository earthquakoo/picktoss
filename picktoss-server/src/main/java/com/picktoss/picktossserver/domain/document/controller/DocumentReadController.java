package com.picktoss.picktossserver.domain.document.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
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
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;

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
            @PathVariable(name = "document_id") Long documentId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo != null ? jwtUserInfo.getMemberId() : null;

        String language = LocaleContextHolder.getLocale().getLanguage();

        GetSingleDocumentResponse documents = documentReadService.findSingleDocument(memberId, documentId, language);
        return ResponseEntity.ok().body(documents);
    }

    @Operation(summary = "모든 문서 가져오기")
    @GetMapping("/documents")
    @ApiErrorCodeExamples({AMAZON_SERVICE_EXCEPTION, DOCUMENT_SORT_OPTION_NOT_SELECT})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetAllDocumentsResponse> getAllDocuments(
            @RequestParam(defaultValue = "CREATED_AT", value = "sort-option") DocumentSortOption documentSortOption
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetAllDocumentsResponse response = documentReadService.findAllDocuments(memberId, documentSortOption);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "북마크된 모든 문서 가져오기")
    @GetMapping("/documents/bookmarked")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetBookmarkedDocumentsResponse> getBookmarkedDocuments(
            @RequestParam(defaultValue = "CREATED_AT", value = "sort-option") BookmarkedDocumentSortOption documentSortOption
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetBookmarkedDocumentsResponse response = documentReadService.findBookmarkedDocuments(memberId, documentSortOption);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "비공개된 모든 문서 가져오기")
    @GetMapping("/documents/not-public")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetIsNotPublicDocumentsResponse> getIsNotPublicDocuments() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        String language = LocaleContextHolder.getLocale().getLanguage();

        GetIsNotPublicDocumentsResponse response = documentReadService.findIsNotPublicDocuments(memberId, language);
        return ResponseEntity.ok().body(response);
    }
}