package com.picktoss.picktossserver.domain.document.controller;

import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExample;
import com.picktoss.picktossserver.domain.document.dto.request.SearchRequest;
import com.picktoss.picktossserver.domain.document.dto.response.GetPublicDocumentsResponse;
import com.picktoss.picktossserver.domain.document.dto.response.SearchDocumentsResponse;
import com.picktoss.picktossserver.domain.document.service.PublicDocumentSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Tag(name = "Document - Public")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class PublicDocumentSearchController {

    private final JwtTokenProvider jwtTokenProvider;
    private final PublicDocumentSearchService publicDocumentSearchService;

    @Operation(summary = "공개된 문서 탐색(탐험)")
    @GetMapping("/documents/public")
    @ResponseStatus(HttpStatus.OK)
    @ApiErrorCodeExample(ErrorInfo.DOCUMENT_PAGE_SET_ERROR)
    public ResponseEntity<GetPublicDocumentsResponse> getPublicDocuments(
            @RequestParam(required = false, value = "category-id") Long categoryId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "page-size", defaultValue = "5") int pageSize
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo != null ? jwtUserInfo.getMemberId() : null;

        String language = LocaleContextHolder.getLocale().getLanguage();
        System.out.println("language = " + language);
        if (!Objects.equals(language, "ko")) {
            language = "en";
        }
        System.out.println("language = " + language);

        GetPublicDocumentsResponse response = publicDocumentSearchService.findPublicDocuments(categoryId, memberId, page, pageSize, language);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "공개된 문서 검색")
    @PostMapping("/documents/public/search")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<SearchDocumentsResponse> searchPublicDocuments(@Valid @RequestBody SearchRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo != null ? jwtUserInfo.getMemberId() : null;

        String language = LocaleContextHolder.getLocale().getLanguage();

        SearchDocumentsResponse response = publicDocumentSearchService.searchPublicDocuments(request.getKeyword(), memberId, language);
        return ResponseEntity.ok().body(response);
    }
}
