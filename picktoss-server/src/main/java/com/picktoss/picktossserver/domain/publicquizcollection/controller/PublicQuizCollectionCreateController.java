package com.picktoss.picktossserver.domain.publicquizcollection.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.publicquizcollection.dto.request.CreatePublicQuizCollectionRequest;
import com.picktoss.picktossserver.domain.publicquizcollection.service.PublicQuizCollectionCreateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "PublicQuizCollection")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class PublicQuizCollectionCreateController {

    private final JwtTokenProvider jwtTokenProvider;
    private final PublicQuizCollectionCreateService publicQuizCollectionCreateService;

    @Operation(summary = "퀴즈 공개하기")
    @PostMapping("/documents/{document_id}/share")
    @ResponseStatus(HttpStatus.CREATED)
    public void createPublicQuizCollection(
            @PathVariable("document_id") Long documentId,
            @Valid @RequestBody CreatePublicQuizCollectionRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        publicQuizCollectionCreateService.createPublicQuizCollection(memberId, documentId, request.getExplanation(), request.getPublicQuizCollectionCategory());
    }
}
