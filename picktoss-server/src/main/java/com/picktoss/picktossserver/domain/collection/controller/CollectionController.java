package com.picktoss.picktossserver.domain.collection.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.collection.controller.request.CreateCollectionRequest;
import com.picktoss.picktossserver.domain.collection.controller.request.UploadRequest;
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

import java.io.IOException;
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
    public ResponseEntity<String> handleFileUpload(
            UploadRequest request
    ) {
        if (request.getFile().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No file uploaded.");
        }

        try {
            // 파일 내용을 String으로 변환
            String fileContent = new String(request.getFile().getBytes(), StandardCharsets.UTF_8);
            System.out.println("File Content: ");
            System.out.println(fileContent); // 파일 내용을 출력

            return ResponseEntity.ok("File uploaded successfully. Content printed in console.");

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed.");
        }
    }

    @Operation(summary = "Create Collection")
    @PostMapping(value = "/collections")
    @ResponseStatus(HttpStatus.OK)
    public void createCollection(@Valid @RequestBody CreateCollectionRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        collectionFacade.createCollection(request.getQuizzes(), request.getName(), request.getDescription(), request.getTag(), request.getEmoji(), request.getCollectionDomain(), memberId);
    }

    @Operation(summary = "Get all Collections")
    @GetMapping("/collections")
    @ResponseStatus(HttpStatus.OK)
    public void findAllCollections() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

    }

    @Operation(summary = "Get Collection by member id")
    @GetMapping("/collection")
    @ResponseStatus(HttpStatus.OK)
    public void findCollectionByMemberId() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<Collection> collections = collectionFacade.findCollectionByMemberId(memberId);
    }

    @Operation(summary = "Get collection by collection id")
    @GetMapping("/collections/{collection_id}")
    @ResponseStatus(HttpStatus.OK)
    public void findCollectionByCollectionId(
            @PathVariable(name = "collection_id") Long collectionId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetSingleCollectionResponse response = collectionFacade.findCollectionByCollectionId(collectionId, memberId);
    }

    @Operation(summary = "Search collections")
    @GetMapping("/collections/{keyword}")
    @ResponseStatus(HttpStatus.OK)
    public void searchCollections(
            @PathVariable(name = "keyword") String keyword
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

//        List<Collection> collections = collectionFacade.searchCollections(keyword);
    }


    @Operation(summary = "Update Collection info")
    @PatchMapping("/collections/info")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCollectionInfo() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

    }

    @Operation(summary = "Update Collection quizzes")
    @PatchMapping("/collections/quizzes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCollectionQuizzes() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();
    }



}
