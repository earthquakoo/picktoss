package com.picktoss.picktossserver.domain.feedback.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.feedback.controller.request.CreateFeedbackRequest;
import com.picktoss.picktossserver.domain.feedback.facade.FeedbackFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Feedback")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class FeedbackController {

    private final JwtTokenProvider jwtTokenProvider;
    private final FeedbackFacade feedbackFacade;

    @Operation(summary = "Create Feedback")
    @PostMapping(value = "/feedback", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void createFeedback(CreateFeedbackRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        feedbackFacade.createFeedback(request.getFile(), request.getTitle(), request.getContent(), request.getType(), request.getEmail(), memberId);
    }
}