package com.picktoss.picktossserver.domain.admin.controller;

import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExamples;
import com.picktoss.picktossserver.domain.admin.dto.request.AdminLoginRequest;
import com.picktoss.picktossserver.domain.admin.dto.request.SignUpRequest;
import com.picktoss.picktossserver.domain.admin.dto.response.AdminLoginResponse;
import com.picktoss.picktossserver.domain.admin.service.AdminAuthCreateService;
import com.picktoss.picktossserver.domain.admin.service.AdminAuthLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.INVALID_PASSWORD;
import static com.picktoss.picktossserver.core.exception.ErrorInfo.MEMBER_NOT_FOUND;

@Tag(name = "Admin - Auth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin")
public class AdminAuthController {

    private final AdminAuthCreateService adminAuthCreateService;
    private final AdminAuthLoginService adminAuthLoginService;

    /**
     * POST
     */

    @Operation(summary = "운영진 회원가입")
    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public void signUp(@Valid @RequestBody SignUpRequest request) {
        adminAuthCreateService.createAdmin(request.getName(), request.getPassword());
    }

    @Operation(summary = "운영진 로그인")
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @ApiErrorCodeExamples({MEMBER_NOT_FOUND, INVALID_PASSWORD})
    public ResponseEntity<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        String accessToken = adminAuthLoginService.login(request.getName(), request.getPassword());
        return ResponseEntity.ok().body(new AdminLoginResponse(accessToken));
    }
}
