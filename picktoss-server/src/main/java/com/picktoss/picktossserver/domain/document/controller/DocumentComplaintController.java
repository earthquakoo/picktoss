package com.picktoss.picktossserver.domain.document.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExamples;
import com.picktoss.picktossserver.domain.document.dto.request.CreateDocumentComplaintRequest;
import com.picktoss.picktossserver.domain.document.entity.DocumentComplaint;
import com.picktoss.picktossserver.domain.document.service.DocumentComplaintCreateService;
import com.picktoss.picktossserver.domain.document.service.DocumentComplaintMessageSendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.DOCUMENT_NOT_FOUND;
import static com.picktoss.picktossserver.core.exception.ErrorInfo.MEMBER_NOT_FOUND;

@Tag(name = "Document")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class DocumentComplaintController {

    private final JwtTokenProvider jwtTokenProvider;
    private final DocumentComplaintCreateService documentComplaintCreateService;
    private final DocumentComplaintMessageSendService documentComplaintMessageSendService;

    @Operation(summary = "문서 신고하기")
    @PostMapping(value = "/documents/{document_id}/complaint", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiErrorCodeExamples({MEMBER_NOT_FOUND, DOCUMENT_NOT_FOUND})
    @ResponseStatus(HttpStatus.CREATED)
    public void createDocumentComplaint(
            @Valid @ModelAttribute CreateDocumentComplaintRequest request,
            @PathVariable("documentId") Long documentId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        DocumentComplaint documentComplaint = documentComplaintCreateService.createDocumentComplaint(memberId, documentId, request.getContent(), request.getComplaintReason(), request.getFiles());
        documentComplaintMessageSendService.sendCollectionComplaintDiscordMessage(documentComplaint);
    }
}
