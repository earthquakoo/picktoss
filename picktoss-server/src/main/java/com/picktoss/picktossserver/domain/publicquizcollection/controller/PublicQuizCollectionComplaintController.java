package com.picktoss.picktossserver.domain.publicquizcollection.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.publicquizcollection.dto.request.CreatePublicQuizCollectionComplaintRequest;
import com.picktoss.picktossserver.domain.publicquizcollection.entity.PublicQuizCollectionComplaint;
import com.picktoss.picktossserver.domain.publicquizcollection.service.PublicQuizCollectionComplaintMessageSendService;
import com.picktoss.picktossserver.domain.publicquizcollection.service.PublicQuizCollectionComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Tag(name = "PublicQuizCollection")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class PublicQuizCollectionComplaintController {

    private final JwtTokenProvider jwtTokenProvider;
    private final PublicQuizCollectionComplaintService publicQuizCollectionComplaintService;
    private final PublicQuizCollectionComplaintMessageSendService publicQuizCollectionComplaintMessageSendService;

    @Operation(summary = "공개 퀴즈 컬렉션 신고하기")
    @PostMapping(value = "/public-quiz-collections/{public_quiz_collection_id}/complaint", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void createPublicQuizCollectionComplaint(
            @Valid @ModelAttribute CreatePublicQuizCollectionComplaintRequest request,
            @PathVariable("public_quiz_collection_id") Long publicQuizCollectionId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        PublicQuizCollectionComplaint publicQuizCollectionComplaint = publicQuizCollectionComplaintService.createPublicQuizCollectionComplaint(publicQuizCollectionId, request.getContent(), memberId, request.getFiles());
        publicQuizCollectionComplaintMessageSendService.sendCollectionComplaintDiscordMessage(publicQuizCollectionComplaint);
    }
}
