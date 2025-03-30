package com.picktoss.picktossserver.domain.publicquizcollection.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.publicquizcollection.service.PublicQuizCollectionBookmarkCreateService;
import com.picktoss.picktossserver.domain.publicquizcollection.service.PublicQuizCollectionBookmarkDeleteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "PublicQuizCollection")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class PublicQuizCollectionBookmarkController {

    private final JwtTokenProvider jwtTokenProvider;
    private final PublicQuizCollectionBookmarkCreateService publicQuizCollectionBookmarkCreateService;
    private final PublicQuizCollectionBookmarkDeleteService publicQuizCollectionBookmarkDeleteService;

    @Operation(summary = "공개된 퀴즈 북마크하기")
    @PostMapping("/public-quiz-collections/{public_quiz_collection_id}/bookmark")
    @ResponseStatus(HttpStatus.CREATED)
    public void createPublicQuizCollectionBookmark(
            @RequestParam(value = "directory-id") Long directoryId,
            @PathVariable("public_quiz_collection_id") Long publicQuizCollectionId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        publicQuizCollectionBookmarkCreateService.createPublicQuizCollectionBookmark(memberId, publicQuizCollectionId, directoryId);

    }

    @Operation(summary = "공개된 퀴즈 북마크 취소하기")
    @DeleteMapping("/public-quiz-collections/{public_quiz_collection_id}/delete")
    @ResponseStatus(HttpStatus.OK)
    public void deletePublicQuizCollectionBookmark(
            @PathVariable("public_quiz_collection_id") Long publicQuizCollectionId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        publicQuizCollectionBookmarkDeleteService.deletePublicQuizCollectionBookmark(memberId, publicQuizCollectionId);
    }

}
