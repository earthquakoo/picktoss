package com.picktoss.picktossserver.domain.document.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExamples;
import com.picktoss.picktossserver.domain.document.dto.request.CreateDocumentRequest;
import com.picktoss.picktossserver.domain.document.dto.request.CreateQuizzesRequest;
import com.picktoss.picktossserver.domain.document.dto.response.CreateDocumentResponse;
import com.picktoss.picktossserver.domain.document.service.DocumentCreateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;
import static com.picktoss.picktossserver.core.exception.ErrorInfo.STAR_SHORTAGE_IN_POSSESSION;

@Tag(name = "Document")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class DocumentCreateController {

    private final JwtTokenProvider jwtTokenProvider;
    private final DocumentCreateService documentCreateService;

    @Operation(summary = "문서 생성")
    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiErrorCodeExamples({CATEGORY_NOT_FOUND, FILE_UPLOAD_ERROR, STAR_SHORTAGE_IN_POSSESSION, MEMBER_NOT_FOUND})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CreateDocumentResponse> createDocument(
            @Valid @ModelAttribute CreateDocumentRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();
        Integer star = Integer.valueOf(request.getStar());

        Long documentId = documentCreateService.createDocument(request.getDocumentName(), request.getEmoji(), request.getCategoryId(), request.getFile(), request.getDocumentType(), request.getQuizType(), request.getIsPublic(), star, memberId);
        return ResponseEntity.ok().body(new CreateDocumentResponse(documentId));
    }

    @Operation(summary = "문서에서 추가 퀴즈 생성")
    @PostMapping("/documents/{document_id}/add-quizzes")
    @ApiErrorCodeExamples({MEMBER_NOT_FOUND, STAR_SHORTAGE_IN_POSSESSION, DOCUMENT_NOT_FOUND})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CreateDocumentResponse> createQuizzes(
            @PathVariable("document_id") Long documentId,
            @Valid @RequestBody CreateQuizzesRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        documentCreateService.createAdditionalQuizzes(documentId, memberId, request.getQuizType(), request.getStar());
        return ResponseEntity.ok().body(new CreateDocumentResponse(documentId));
    }
}
