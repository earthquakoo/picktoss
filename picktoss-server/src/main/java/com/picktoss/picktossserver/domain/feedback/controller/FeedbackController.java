package com.picktoss.picktossserver.domain.feedback.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExamples;
import com.picktoss.picktossserver.domain.feedback.controller.request.CreateFeedbackRequest;
import com.picktoss.picktossserver.domain.feedback.facade.FeedbackFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.AMAZON_SERVICE_EXCEPTION;
import static com.picktoss.picktossserver.core.exception.ErrorInfo.MEMBER_NOT_FOUND;

@Tag(name = "Feedback")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class FeedbackController {

    private final JwtTokenProvider jwtTokenProvider;
    private final FeedbackFacade feedbackFacade;

    @Operation(summary = "Create Feedback")
    @PostMapping(value = "/feedback", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiErrorCodeExamples({MEMBER_NOT_FOUND, AMAZON_SERVICE_EXCEPTION})
    @ResponseStatus(HttpStatus.CREATED)
    public void createFeedback(@Valid @ModelAttribute CreateFeedbackRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        feedbackFacade.createFeedback(request.getFiles(), request.getTitle(), request.getContent(), request.getType(), request.getEmail(), memberId);
    }
}