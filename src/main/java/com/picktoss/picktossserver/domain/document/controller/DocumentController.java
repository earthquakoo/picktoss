package com.picktoss.picktossserver.domain.document.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.s3.S3Provider;
import com.picktoss.picktossserver.domain.document.controller.request.MoveDocumentToCategoryRequest;
import com.picktoss.picktossserver.domain.document.controller.request.SearchDocumentNameRequest;
import com.picktoss.picktossserver.domain.document.controller.request.ChangeDocumentsOrderRequest;
import com.picktoss.picktossserver.domain.document.controller.response.*;
import com.picktoss.picktossserver.domain.document.controller.request.CreateDocumentRequest;
import com.picktoss.picktossserver.domain.document.facade.DocumentFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "3. Document")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class DocumentController {

    private final S3Provider s3Provider;
    private final JwtTokenProvider jwtTokenProvider;
    private final DocumentFacade documentFacade;

    @Operation(summary = "Create document")
    @PostMapping("/documents")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CreateDocumentResponse> createDocument(
            CreateDocumentRequest request
            ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();
        Long categoryId = Long.valueOf(request.getCategoryId());

        Long documentId = documentFacade.saveDocument(request.getUserDocumentName(), request.getFile(), memberId, categoryId);
        return ResponseEntity.ok().body(new CreateDocumentResponse(documentId));
    }

    @Operation(summary = "Get document by id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Get document success!",
                            content = @Content(schema = @Schema(implementation = GetSingleDocumentResponse.class)))})
    @GetMapping("/documents/{document_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetSingleDocumentResponse> getSingleDocument(
            @PathVariable(name = "document_id") Long documentId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetSingleDocumentResponse documents = documentFacade.findSingleDocument(memberId, documentId);
        return ResponseEntity.ok().body(documents);
    }

    @Operation(summary = "Get all documents by category id",
            responses = {
            @ApiResponse(responseCode = "200", description = "Get all document success!",
                    content = @Content(schema = @Schema(implementation = GetAllDocumentsResponse.class)))})
    @GetMapping("/categories/{category_id}/documents")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetAllDocumentsResponse> getAllDocuments(@PathVariable(name = "category_id") Long categoryId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<GetAllDocumentsResponse.GetAllDocumentsDocumentDto> allDocuments = documentFacade.findAllDocuments(memberId, categoryId);
        return ResponseEntity.ok().body(new GetAllDocumentsResponse(allDocuments));
    }

    @GetMapping("/categories/{category_id}/documents/sort")
    @ResponseStatus(HttpStatus.OK)
    public void changeDocumentSort() {

    }

    @Operation(summary = "Get document by file name")
    @PostMapping("/documents/search")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<SearchDocumentNameResponse> searchDocumentName(@Valid @RequestBody SearchDocumentNameRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        SearchDocumentNameResponse response = documentFacade.searchDocumentName(request.getWord(), memberId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Get most incorrect top 5 document")
    @GetMapping("/documents/top-five")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetMostIncorrectDocumentsResponse> getMostIncorrectDocuments() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetMostIncorrectDocumentsResponse response = documentFacade.findMostIncorrectDocuments(memberId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Delete document by id")
    @DeleteMapping("/documents/{document_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(@PathVariable(name = "document_id") Long documentId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        documentFacade.deleteDocument(memberId, documentId);
    }

    @Operation(summary = "Change document order")
    @PatchMapping("/documents/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeDocumentsOrder(@Valid @RequestBody ChangeDocumentsOrderRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        documentFacade.changeDocumentOrder(request.getDocuments(), memberId);
    }

    @Operation(summary = "Move document to category")
    @PatchMapping("/documents/move")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void moveDocumentToCategory(@Valid @RequestBody MoveDocumentToCategoryRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        documentFacade.moveDocumentToCategory(request.getDocumentId(), request.getCategoryId(), memberId);
    }
}
