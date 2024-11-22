package com.picktoss.picktossserver.domain.admin.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin")
public class AdminController {

    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "모든 사용자 조회")
    @GetMapping("/members")
    public void getMembers() {
        
    }

    @Operation(summary = "컬렉션 퀴즈 만들기")
    @PostMapping("/quizzes")
    public void createQuizInCollection() {

    }
}
