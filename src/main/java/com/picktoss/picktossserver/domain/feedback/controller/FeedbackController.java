package com.picktoss.picktossserver.domain.feedback.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.feedback.controller.request.CreateFeedbackRequest;
import com.picktoss.picktossserver.domain.feedback.facade.FeedbackFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Tag(name = "4. Feedback")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class FeedbackController {

    private final JwtTokenProvider jwtTokenProvider;
    private final FeedbackFacade feedbackFacade;

    @Operation(summary = "Create Feedback")
    @PostMapping("/feedback")
    @ResponseStatus(HttpStatus.OK)
    public void createFeedback(@Valid @RequestBody CreateFeedbackRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        feedbackFacade.createFeedback(request.getContent(), memberId);
    }
}
