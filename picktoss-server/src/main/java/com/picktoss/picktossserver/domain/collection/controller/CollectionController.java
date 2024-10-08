package com.picktoss.picktossserver.domain.collection.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.collection.controller.dto.CollectionResponseDto;
import com.picktoss.picktossserver.domain.collection.controller.mapper.CollectionMapper;
import com.picktoss.picktossserver.domain.collection.controller.request.CreateCollectionRequest;
import com.picktoss.picktossserver.domain.collection.controller.request.UpdateCollectionInfoRequest;
import com.picktoss.picktossserver.domain.collection.controller.request.UpdateCollectionQuizzesRequest;
import com.picktoss.picktossserver.domain.collection.controller.request.UploadRequest;
import com.picktoss.picktossserver.domain.collection.controller.response.GetAllCollectionsResponse;
import com.picktoss.picktossserver.domain.collection.controller.response.GetSingleCollectionResponse;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.facade.CollectionFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag(name = "Collection")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class CollectionController {

    private final JwtTokenProvider jwtTokenProvider;
    private final CollectionFacade collectionFacade;

    @PostMapping(value = "/upload")
    @ResponseStatus(HttpStatus.OK)
    public String handleFileUpload(
            UploadRequest request
    ) {

        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(request.getFile().getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "파일 읽기에 실패했습니다.";
        }
        return content.toString();
    }

    @Operation(summary = "Create Collection")
    @PostMapping(value = "/collection/collections")
    @ResponseStatus(HttpStatus.OK)
    public void createCollection(@Valid @RequestBody CreateCollectionRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        collectionFacade.createCollection(request.getQuizzes(), request.getName(), request.getDescription(), request.getTag(), request.getEmoji(), request.getCollectionDomain(), memberId);
    }

    @Operation(summary = "Get all Collections")
    @GetMapping("/collection/collections")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetAllCollectionsResponse> findAllCollections(
            @RequestParam(required = false, defaultValue = "createdAt", value = "collection-sort-option") String collectionSortOption,
            @RequestParam(required = false, value = "collection-domain-option") List<String> collectionDomainOption,
            @RequestParam(required = false, value = "quiz-type") String quizType,
            @RequestParam(required = false, value = "quiz-count") Integer quizCount
            ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        System.out.println("collectionSortOption = " + collectionSortOption);
        System.out.println("collectionDomainOption = " + collectionDomainOption);
        System.out.println("quizType = " + quizType);
        System.out.println("quizCount = " + quizCount);

        List<GetAllCollectionsResponse.GetAllCollectionsDto> response = collectionFacade.findAllCollections(collectionSortOption, collectionDomainOption, quizType, quizCount);
        return ResponseEntity.ok().body(new GetAllCollectionsResponse(response));
    }

    // my collection
    @Operation(summary = "Get Collection by member id")
    @GetMapping("/collection/get-collection")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CollectionResponseDto> findCollectionByMemberId() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<Collection> collections = collectionFacade.findCollectionByMemberId(memberId);
        CollectionResponseDto response = CollectionMapper.collectionsToCollectionResponseDto(collections);
        return ResponseEntity.ok().body(response);
    }

    // collection 상세 정보
    @Operation(summary = "Get collection by collection id")
    @GetMapping("/collections/{collection_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetSingleCollectionResponse> findCollectionByCollectionId(
            @PathVariable(name = "collection_id") Long collectionId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetSingleCollectionResponse response = collectionFacade.findCollectionByCollectionId(collectionId, memberId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Search collections")
    @GetMapping("/collection/collections/{keyword}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CollectionResponseDto> searchCollections(
            @PathVariable(name = "keyword") String keyword
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<Collection> collections = collectionFacade.searchCollections(keyword);
        CollectionResponseDto response = CollectionMapper.collectionsToCollectionResponseDto(collections);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Delete collection")
    @DeleteMapping("/collections/{collection_id}/delete-collection")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCollection(
            @PathVariable(name = "collection_id") Long collectionId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        collectionFacade.deleteCollection(collectionId, memberId);
    }


    @Operation(summary = "Update Collection info")
    @PatchMapping("/collections/{collection_id}/update-info")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCollectionInfo(
            @PathVariable(name = "collection_id") Long collectionId,
            @Valid @RequestBody UpdateCollectionInfoRequest request
            ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        collectionFacade.updateCollectionInfo(collectionId, memberId, request.getName(), request.getTag(), request.getDescription(), request.getEmoji(), request.getCollectionDomain());
    }

    @Operation(summary = "Update Collection quizzes")
    @PatchMapping("/collections/{collection_id}/update-quizzes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCollectionQuizzes(
            @PathVariable(name = "collection_id") Long collectionId,
            @Valid @RequestBody UpdateCollectionQuizzesRequest request
            ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        collectionFacade.updateCollectionQuizzes(request.getQuizzes(), collectionId, memberId);
    }
}
