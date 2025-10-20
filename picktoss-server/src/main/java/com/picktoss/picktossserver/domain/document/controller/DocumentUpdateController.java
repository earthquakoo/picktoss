package com.picktoss.picktossserver.domain.document.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExample;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExamples;
import com.picktoss.picktossserver.domain.document.dto.request.*;
import com.picktoss.picktossserver.domain.document.service.DocumentUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;

@Tag(name = "Document")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class DocumentUpdateController {

    private final JwtTokenProvider jwtTokenProvider;
    private final DocumentUpdateService documentUpdateService;

    @Operation(summary = "문서 내용 업데이트")
    @PatchMapping(value = "/documents/{document_id}/update-content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiErrorCodeExamples({DOCUMENT_NOT_FOUND, AMAZON_SERVICE_EXCEPTION})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeDocumentContent(
            @PathVariable(name = "document_id") Long documentId,
            @Valid @ModelAttribute UpdateDocumentContentRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        String language = LocaleContextHolder.getLocale().getLanguage();

        documentUpdateService.updateDocumentContent(request.getFile(), documentId, memberId, request.getName(), language);
    }

    @Operation(summary = "문서 이름 변경")
    @PatchMapping("/documents/{document_id}/update-name")
    @ApiErrorCodeExample(DOCUMENT_NOT_FOUND)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateDocumentName(
            @PathVariable(name = "document_id") Long documentId,
            @Valid @RequestBody UpdateDocumentNameRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        String language = LocaleContextHolder.getLocale().getLanguage();

        documentUpdateService.updateDocumentName(documentId, memberId, request.getName(), language);
    }

    @Operation(summary = "문서 카테고리 변경")
    @PatchMapping("/documents/{document_id}/update-category")
    @ApiErrorCodeExamples({DOCUMENT_NOT_FOUND, CATEGORY_NOT_FOUND})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateDocumentCategory(
            @PathVariable("document_id") Long documentId,
            @Valid @RequestBody UpdateDocumentCategoryRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        String language = LocaleContextHolder.getLocale().getLanguage();

        documentUpdateService.updateDocumentCategory(documentId, memberId, request.getCategoryId(), language);
    }

    @Operation(summary = "문서 이모지 변경")
    @PatchMapping("/documents/{document_id}/update-emoji")
    @ApiErrorCodeExample(DOCUMENT_NOT_FOUND)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateDocumentEmoji(
            @PathVariable("document_id") Long documentId,
            @Valid @RequestBody UpdateDocumentEmojiRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        String language = LocaleContextHolder.getLocale().getLanguage();

        documentUpdateService.updateDocumentEmoji(documentId, memberId, request.getEmoji(), language);
    }

    @Operation(summary = "문서 공개여부 변경")
    @PatchMapping("/documents/{document_id}/update-public")
    @ApiErrorCodeExamples({DOCUMENT_NOT_FOUND, QUIZ_GENERATION_FAILED})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateDocumentIsPublic(
            @PathVariable("document_id") Long documentId,
            @Valid @RequestBody UpdateDocumentIsPublicRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        String language = LocaleContextHolder.getLocale().getLanguage();

        documentUpdateService.updateDocumentIsPublic(documentId, memberId, request.getIsPublic(), language);
    }
}
