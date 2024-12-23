package com.picktoss.picktossserver.domain.admin.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.domain.admin.controller.request.CreateCollectionForAdminRequest;
import com.picktoss.picktossserver.domain.admin.controller.response.GetCollectionsForAdminResponse;
import com.picktoss.picktossserver.domain.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin")
public class AdminController {

    private final JwtTokenProvider jwtTokenProvider;
    private final AdminService adminService;

    /**
     * GET
     */

    @Operation(summary = "컬렉션 관리")
    @GetMapping("/collections")
    public ResponseEntity<GetCollectionsForAdminResponse> getCollections() {
        GetCollectionsForAdminResponse response = adminService.findCollections();
        return ResponseEntity.ok().body(response);
    }

    /**
     * POST
     */

    @Operation(summary = "컬렉션 만들기")
    @PostMapping("/collections")
    public void createQuizInCollection(
            @Valid @RequestBody CreateCollectionForAdminRequest request
    ) {

    }
}
