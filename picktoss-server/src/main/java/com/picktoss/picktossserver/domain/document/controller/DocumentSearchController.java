package com.picktoss.picktossserver.domain.document.controller;


import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExample;
import com.picktoss.picktossserver.domain.document.dto.request.SearchRequest;
import com.picktoss.picktossserver.domain.document.dto.response.SearchDocumentsResponse;
import com.picktoss.picktossserver.domain.document.service.DocumentSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.AMAZON_SERVICE_EXCEPTION;

@Tag(name = "Document")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class DocumentSearchController {

    private final JwtTokenProvider jwtTokenProvider;
    private final DocumentSearchService documentSearchService;

    @Operation(summary = "문서 검색")
    @PostMapping("/documents/search")
    @ApiErrorCodeExample(AMAZON_SERVICE_EXCEPTION)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<SearchDocumentsResponse> searchDocumentsByKeyword(@Valid @RequestBody SearchRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        SearchDocumentsResponse response = documentSearchService.searchDocumentsByKeyword(request.getKeyword(), memberId);
        return ResponseEntity.ok().body(response);
    }
}
