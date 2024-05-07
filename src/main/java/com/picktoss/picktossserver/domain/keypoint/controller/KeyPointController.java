package com.picktoss.picktossserver.domain.keypoint.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.keypoint.controller.response.GetAllDocumentKeyPointsResponse;
import com.picktoss.picktossserver.domain.keypoint.facade.KeyPointFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "4. Key Point")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class KeyPointController {

    private final KeyPointFacade keyPointFacade;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "Get all category keypoint by document")
    @GetMapping("/categories/{category_id}/documents/key-point")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetAllDocumentKeyPointsResponse> getAllCategoryQuestions(@PathVariable("category_id") Long categoryId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<GetAllDocumentKeyPointsResponse.GetAllDocumentDto> allCategoryQuestions = keyPointFacade.findAllCategoryKeyPoints(categoryId, memberId);
        return ResponseEntity.ok().body(new GetAllDocumentKeyPointsResponse(allCategoryQuestions));
    }
}
