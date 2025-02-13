package com.picktoss.picktossserver.domain.subscription.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Subscription")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class SubscriptionController {

    @Operation(summary = "구독 취소하기")
    public void cancelSubscription() {

    }
}
