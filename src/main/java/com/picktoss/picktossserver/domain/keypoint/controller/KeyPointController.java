package com.picktoss.picktossserver.domain.keypoint.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.keypoint.controller.request.UpdateBookmarkKeypointRequest;
import com.picktoss.picktossserver.domain.keypoint.controller.response.GetAllDocumentKeyPointsResponse;
import com.picktoss.picktossserver.domain.keypoint.controller.response.GetBookmarkedKeyPointResponse;
import com.picktoss.picktossserver.domain.keypoint.facade.KeyPointFacade;
import com.picktoss.picktossserver.domain.quiz.controller.dto.QuizResponseDto;
import com.picktoss.picktossserver.domain.quiz.controller.mapper.QuizMapper;
import com.picktoss.picktossserver.domain.quiz.controller.request.UpdateBookmarkQuizRequest;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    @Operation(summary = "Get bookmarked keypoint")
    @GetMapping("/key-point/bookmark")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetBookmarkedKeyPointResponse> getBookmarkedKeypoint() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<GetBookmarkedKeyPointResponse.GetBookmarkedKeyPointDto> keyPointDtos = keyPointFacade.findBookmarkedKeyPoint();
        return ResponseEntity.ok().body(new GetBookmarkedKeyPointResponse(keyPointDtos));
    }

    @Operation(summary = "Update bookmarked keypoint")
    @PatchMapping("/key-point/{key_point_id}/bookmark")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateBookmarkKeypoint(
            @Valid @RequestBody UpdateBookmarkKeypointRequest request,
            @PathVariable("key_point_id") Long keyPointId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        keyPointFacade.updateBookmarkKeypoint(keyPointId, request.isBookmark());
    }
}
