package com.picktoss.picktossserver.domain.collection.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExample;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExamples;
import com.picktoss.picktossserver.domain.collection.controller.dto.CollectionResponseDto;
import com.picktoss.picktossserver.domain.collection.controller.mapper.CollectionDtoMapper;
import com.picktoss.picktossserver.domain.collection.controller.request.*;
import com.picktoss.picktossserver.domain.collection.controller.response.*;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.facade.CollectionFacade;
import com.picktoss.picktossserver.global.enums.collection.CollectionCategory;
import com.picktoss.picktossserver.global.enums.collection.CollectionSortOption;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;

@Tag(name = "Collection")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class CollectionController {

    private final JwtTokenProvider jwtTokenProvider;
    private final CollectionFacade collectionFacade;

    /**
     * GET
     */

    @Operation(summary = "모든 컬렉션 가져오기(탐색)")
    @GetMapping("/collections")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CollectionResponseDto> getAllCollections(
            @RequestParam(required = false, defaultValue = "POPULARITY", value = "collection-sort-option") CollectionSortOption collectionSortOption,
            @RequestParam(required = false, value = "collection-category") List<CollectionCategory> collectionCategoryOption,
            @RequestParam(required = false, value = "quiz-type") QuizType quizType,
            @RequestParam(required = false, value = "quiz-count") Integer quizCount
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<Collection> collections = collectionFacade.findAllCollections(collectionSortOption, collectionCategoryOption, quizType, quizCount);
        CollectionResponseDto response = CollectionDtoMapper.collectionsToCollectionResponseDto(collections);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "북마크한 컬렉션 가져오기")
    @GetMapping("/collections/bookmarked-collections")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CollectionResponseDto> getAllByMemberIdAndBookmarked() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<Collection> collections = collectionFacade.findAllByMemberIdAndBookmarked(memberId);
        CollectionResponseDto response = CollectionDtoMapper.collectionsToCollectionResponseDto(collections);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "북마크하거나 소유한 컬렉션 분야별로 모든 퀴즈 랜덤하게 가져오기")
    @GetMapping("/collections/{collection_category}/quizzes")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetQuizzesInCollectionByCollectionCategory> getQuizzesInCollectionByCollectionCategory(
            @PathVariable("collection_category") CollectionCategory collectionCategory
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<GetQuizzesInCollectionByCollectionCategory.QuizInCollectionDto> response = collectionFacade.findAllByMemberIdAndCollectionCategoryAndBookmarked(memberId, collectionCategory);
        return ResponseEntity.ok().body(new GetQuizzesInCollectionByCollectionCategory(response));
    }

    @Operation(summary = "직접 생성한 컬렉션 가져오기")
    @GetMapping("/collections/my-collections")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CollectionResponseDto> getAllByMemberId() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<Collection> collections = collectionFacade.findMemberGeneratedCollections(memberId);
        CollectionResponseDto response = CollectionDtoMapper.collectionsToCollectionResponseDto(collections);
        return ResponseEntity.ok().body(response);
    }

    // collection 상세 정보
    @Operation(summary = "컬렉션 상세 정보 가져오기")
    @GetMapping("/collections/{collection_id}/info")
    @ApiErrorCodeExample(COLLECTION_NOT_FOUND)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetSingleCollectionResponse> getCollectionInfoByCollectionId(
            @PathVariable(name = "collection_id") Long collectionId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetSingleCollectionResponse response = collectionFacade.findCollectionInfoByCollectionId(collectionId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "컬렉션 검색하기")
    @GetMapping("/collections/{keyword}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CollectionResponseDto> searchCollections(
            @PathVariable(name = "keyword") String keyword
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<Collection> collections = collectionFacade.searchCollections(keyword);
        CollectionResponseDto response = CollectionDtoMapper.collectionsToCollectionResponseDto(collections);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "컬렉션 분석")
    @GetMapping("/collections/analysis")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetCollectionSAnalysisResponse> getCollectionAnalysis() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetCollectionSAnalysisResponse response = collectionFacade.findCollectionsAnalysis(memberId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "사용자 관심 분야 컬렉션 가져오기")
    @GetMapping("/collections/interest-category-collection")
    @ApiErrorCodeExample(MEMBER_NOT_FOUND)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CollectionResponseDto> getInterestCategoryCollections() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<Collection> collections = collectionFacade.findInterestCategoryCollections(memberId);
        CollectionResponseDto response = CollectionDtoMapper.collectionsToCollectionResponseDto(collections);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "사용자가 북마크했거나 생성한 컬렉션 카테고리 가져오기")
    @GetMapping("/collections/categories")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetCollectionCategoriesResponse> getCollectionCategories() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<GetCollectionCategoriesResponse.GetCollectionCategoriesDto> response = collectionFacade.findCollectionCategoriesByMemberId(memberId);
        return ResponseEntity.ok().body(new GetCollectionCategoriesResponse(response));
    }


    /**
     * POST
     */

    @Operation(summary = "컬렉션 생성")
    @PostMapping(value = "/collections")
    @ApiErrorCodeExamples({MEMBER_NOT_FOUND, QUIZ_NOT_FOUND_ERROR})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CreateCollectionResponse> createCollection(@Valid @RequestBody CreateCollectionRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        Long collectionId = collectionFacade.createCollection(request.getQuizzes(), request.getName(), request.getDescription(), request.getEmoji(), request.getCollectionCategory(), memberId);
        return ResponseEntity.ok().body(new CreateCollectionResponse(collectionId));
    }

    @Operation(summary = "컬렉션 북마크하기")
    @PostMapping("/collections/{collection_id}/create-bookmark")
    @ApiErrorCodeExamples({COLLECTION_NOT_FOUND, OWN_COLLECTION_CANT_BOOKMARK, MEMBER_NOT_FOUND})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createCollectionBookmark(
            @PathVariable("collection_id") Long collectionId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        collectionFacade.createCollectionBookmark(memberId, collectionId);
    }

    @Operation(summary = "컬렉션 신고하기")
    @PostMapping(value = "/collections/{collection_id}/complaint", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void createCollectionComplaint(
            @Valid @ModelAttribute CreateCollectionComplaintRequest request,
            @PathVariable("collection_id") Long collectionId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        collectionFacade.createCollectionComplaint(request.getFiles(), request.getContent(), collectionId, memberId);
    }

    /**
     * PATCH
     */

    @Operation(summary = "컬렉션 정보 수정")
    @PatchMapping("/collections/{collection_id}/update-info")
    @ApiErrorCodeExample(COLLECTION_NOT_FOUND)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCollectionInfo(
            @PathVariable(name = "collection_id") Long collectionId,
            @Valid @RequestBody UpdateCollectionInfoRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        collectionFacade.updateCollectionInfo(collectionId, memberId, request.getName(), request.getDescription(), request.getEmoji(), request.getCollectionCategory());
    }

    @Operation(summary = "컬렉션에 퀴즈 추가", description = "노트 상세에서 특정 퀴즈를 특정 컬렉션에 추가")
    @PatchMapping("/collection/{collection_id}/add-quiz")
    @ApiErrorCodeExamples({QUIZ_NOT_FOUND_ERROR, COLLECTION_NOT_FOUND, DUPLICATE_QUIZ_IN_COLLECTION})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addQuizToCollection(
            @PathVariable(name = "collection_id") Long collectionId,
            @Valid @RequestBody AddQuizToCollectionRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        collectionFacade.addQuizToCollection(collectionId, memberId, request.getQuizId());
    }

    @Operation(summary = "컬렉션 문제 편집")
    @PatchMapping("/collections/{collection_id}/update-quizzes")
    @ApiErrorCodeExamples({COLLECTION_NOT_FOUND, QUIZ_NOT_FOUND_ERROR})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCollectionQuizzes(
            @PathVariable(name = "collection_id") Long collectionId,
            @Valid @RequestBody UpdateCollectionQuizzesRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        collectionFacade.updateCollectionQuizzes(request.getQuizzes(), collectionId, memberId);
    }

    /**
     * DELETE
     */

    @Operation(summary = "컬렉션 삭제")
    @DeleteMapping("/collections/{collection_id}/delete-collection")
    @ApiErrorCodeExample(COLLECTION_NOT_FOUND)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCollection(
            @PathVariable(name = "collection_id") Long collectionId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        collectionFacade.deleteCollection(collectionId, memberId);
    }

    @Operation(summary = "컬렉션 북마크 취소하기")
    @DeleteMapping("/collections/{collection_id}/delete-bookmark")
    @ApiErrorCodeExamples({COLLECTION_NOT_FOUND, COLLECTION_BOOKMARK_NOT_FOUND, MEMBER_NOT_FOUND})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCollectionBookmark(
            @PathVariable("collection_id") Long collectionId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        collectionFacade.deleteCollectionBookmark(memberId, collectionId);
    }
}
