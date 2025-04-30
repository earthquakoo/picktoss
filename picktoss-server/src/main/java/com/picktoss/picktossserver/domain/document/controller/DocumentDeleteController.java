package com.picktoss.picktossserver.domain.document.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExample;
import com.picktoss.picktossserver.domain.document.dto.request.DeleteDocumentRequest;
import com.picktoss.picktossserver.domain.document.service.DocumentDeleteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.DOCUMENT_NOT_FOUND;

@Tag(name = "Document")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class DocumentDeleteController {

    private final JwtTokenProvider jwtTokenProvider;
    private final DocumentDeleteService documentDeleteService;

    @Operation(summary = "문서 삭제")
    @DeleteMapping("/documents/delete")
    @ApiErrorCodeExample(DOCUMENT_NOT_FOUND)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(
            @Valid @RequestBody DeleteDocumentRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        documentDeleteService.deleteDocument(memberId, request.getDocumentIds());
    }
}
