package com.picktoss.picktossserver.domain.document.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.document.controller.request.*;
import com.picktoss.picktossserver.domain.document.controller.response.*;
import com.picktoss.picktossserver.domain.document.facade.DocumentFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "3. Document")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class DocumentController {

    private final JwtTokenProvider jwtTokenProvider;
    private final DocumentFacade documentFacade;

    @Operation(summary = "Create document")
    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CreateDocumentResponse> createDocument(
            @RequestPart(value = "file") MultipartFile file,
            @RequestPart(value = "request", required = false) CreateDocumentRequest request
            ) { 
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        Long documentId = documentFacade.saveDocument(request.getDocumentName(), file, memberId, request.getCategoryId());
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

    @Operation(summary = "Get document search result")
    @PostMapping("/documents/search")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<SearchDocumentResponse> searchDocumentName(@Valid @RequestBody SearchDocumentNameRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<SearchDocumentResponse.SearchDocumentDto> documents = documentFacade.searchDocument(request.getWord(), memberId);
        return ResponseEntity.ok().body(new SearchDocumentResponse(documents));
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
    public void changeDocumentsOrder(@Valid @RequestBody UpdateDocumentsOrderRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        documentFacade.changeDocumentOrder(request.getDocumentId(), request.getPreDragDocumentOrder(), request.getAfterDragDocumentOrder(), memberId);
    }

    @Operation(summary = "Move document to category")
    @PatchMapping("/documents/move")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void moveDocumentToCategory(@Valid @RequestBody MoveDocumentToCategoryRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        documentFacade.moveDocumentToCategory(request.getDocumentId(), request.getCategoryId(), memberId);
    }

    @Operation(summary = "Update document content")
    @PatchMapping("/documents/{document_id}/update-content")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateDocumentContent(
            @PathVariable(name = "document_id") Long documentId,
            @RequestPart UpdateDocumentContentRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        documentFacade.updateDocumentContent(documentId, memberId, request.getFile());
    }

    @Operation(summary = "Re-upload document")
    @PostMapping("/documents/{document_id}/re-upload")
    @ResponseStatus(HttpStatus.OK)
    public void reUploadDocument(@PathVariable(name = "document_id") Long documentId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        documentFacade.reUploadDocument(documentId, memberId);
    }
}
