package com.picktoss.picktossserver.domain.publicquizcollection.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.publicquizcollection.dto.mapper.PublicQuizCollectionDtoMapper;
import com.picktoss.picktossserver.domain.publicquizcollection.dto.mapper.PublicQuizCollectionResponseDto;
import com.picktoss.picktossserver.domain.publicquizcollection.entity.PublicQuizCollection;
import com.picktoss.picktossserver.domain.publicquizcollection.service.PublicQuizCollectionSearchService;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "PublicQuizCollection")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class PublicQuizCollectionSearchController {

    private final JwtTokenProvider jwtTokenProvider;
    private final PublicQuizCollectionSearchService publicQuizCollectionSearchService;

    @Operation(summary = "모든 공개 퀴즈 가져오기")
    @GetMapping("/public-quiz-collections")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PublicQuizCollectionResponseDto> findAllPublicQuizCollections(
            @RequestParam(required = false, value = "quiz-type") QuizType quizType,
            @RequestParam(required = false, value = "quiz-count") Integer quizCount
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<PublicQuizCollection> publicQuizCollections = publicQuizCollectionSearchService.findAllPublicQuizCollections(memberId, quizType, quizCount);
        PublicQuizCollectionResponseDto response = PublicQuizCollectionDtoMapper.collectionsToPublicQuizCollectionDto(publicQuizCollections);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "북마크한 공개 퀴즈 가져오기")
    @GetMapping("/public-quiz-collections/bookmarked")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PublicQuizCollectionResponseDto> findAllPublicQuizCollectionsByBookmarked() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<PublicQuizCollection> publicQuizCollections = publicQuizCollectionSearchService.findAllByBookmarked(memberId);
        PublicQuizCollectionResponseDto response = PublicQuizCollectionDtoMapper.collectionsToPublicQuizCollectionDto(publicQuizCollections);
        return ResponseEntity.ok().body(response);
    }
}
