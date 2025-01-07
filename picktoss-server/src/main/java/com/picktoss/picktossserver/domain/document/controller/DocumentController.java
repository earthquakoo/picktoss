package com.picktoss.picktossserver.domain.document.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExample;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExamples;
import com.picktoss.picktossserver.domain.document.dto.request.*;
import com.picktoss.picktossserver.domain.document.dto.response.*;
import com.picktoss.picktossserver.domain.document.service.DocumentCreateService;
import com.picktoss.picktossserver.domain.document.service.DocumentDeleteService;
import com.picktoss.picktossserver.domain.document.service.DocumentSearchService;
import com.picktoss.picktossserver.domain.document.service.DocumentUpdateService;
import com.picktoss.picktossserver.global.enums.document.DocumentSortOption;
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

@Tag(name = "Document")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class DocumentController {

    private final JwtTokenProvider jwtTokenProvider;
    private final DocumentCreateService documentCreateService;
    private final DocumentDeleteService documentDeleteService;
    private final DocumentSearchService documentSearchService;
    private final DocumentUpdateService documentUpdateService;

    /**
     * GET
     */

    @Operation(summary = "document_id로 문서 가져오기")
    @GetMapping("/documents/{document_id}")
    @ApiErrorCodeExamples({DOCUMENT_NOT_FOUND, AMAZON_SERVICE_EXCEPTION})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetSingleDocumentResponse> getSingleDocument(
            @PathVariable(name = "document_id") Long documentId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetSingleDocumentResponse documents = documentSearchService.findSingleDocument(memberId, documentId);
        return ResponseEntity.ok().body(documents);
    }

    @Operation(summary = "모든 문서 가져오기")
    @GetMapping("/directories/documents")
    @ApiErrorCodeExample(AMAZON_SERVICE_EXCEPTION)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetAllDocumentsResponse> getAllDocuments(
            @RequestParam(required = false, value = "directory-id") Long directoryId,
            @RequestParam(defaultValue = "CREATE_AT", value = "sort-option") DocumentSortOption documentSortOption) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<GetAllDocumentsResponse.GetAllDocumentsDocumentDto> allDocuments = documentSearchService.findAllDocumentsInDirectory(memberId, directoryId, documentSortOption);
        return ResponseEntity.ok().body(new GetAllDocumentsResponse(allDocuments));
    }

    @Operation(summary = "복습 필수 노트 top 5")
    @GetMapping("/documents/review-need-documents")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetDocumentsNeedingReviewResponse> getDocumentsNeedingReview() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetDocumentsNeedingReviewResponse response = documentSearchService.findDocumentsNeedingReview(memberId);
        return ResponseEntity.ok().body(response);
    }

    /**
     * POST
     */

    @Operation(summary = "문서 생성")
    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiErrorCodeExamples({DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR, DIRECTORY_NOT_FOUND, FILE_UPLOAD_ERROR, STAR_SHORTAGE_IN_POSSESSION})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CreateDocumentResponse> createDocument(
            @Valid @ModelAttribute CreateDocumentRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();
        Long directoryId = Long.valueOf(request.getDirectoryId());
        Integer star = Integer.valueOf(request.getStar());

        Long documentId = documentCreateService.createDocument(request.getDocumentName(), request.getFile(), request.getDocumentType(), request.getQuizType(), star, directoryId, memberId);
        return ResponseEntity.ok().body(new CreateDocumentResponse(documentId));
    }

    @Operation(summary = "문서 검색")
    @PostMapping("/documents/search")
    @ApiErrorCodeExample(AMAZON_SERVICE_EXCEPTION)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<SearchDocumentResponse> searchDocumentByKeyword(@Valid @RequestBody SearchRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        SearchDocumentResponse response = documentSearchService.searchDocumentByKeyword(request.getKeyword(), memberId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "통합(문서, 컬렉션, 퀴즈) 검색")
    @PostMapping("/integrated-search")
    @ApiErrorCodeExample(AMAZON_SERVICE_EXCEPTION)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<IntegratedSearchResponse> integratedSearchByKeyword(
            @Valid @RequestBody SearchRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        IntegratedSearchResponse response = documentSearchService.integratedSearchByKeyword(memberId, request.getKeyword());
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "문서에서 추가 퀴즈 생성")
    @PostMapping("/documents/{document_id}/add-quizzes")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CreateDocumentResponse> createQuizzes(
            @PathVariable("document_id") Long documentId,
            @Valid @RequestBody CreateQuizzesRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        documentCreateService.createAdditionalQuizzes(documentId, memberId, request.getQuizType(), request.getStar());
        return ResponseEntity.ok().body(new CreateDocumentResponse(documentId));
    }

    /**
     * PATCH
     */

    @Operation(summary = "문서 다른 폴더로 옮기기")
    @PatchMapping("/documents/move")
    @ApiErrorCodeExample(DIRECTORY_NOT_FOUND)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void moveDocumentToDirectory(@Valid @RequestBody MoveDocumentToDirectoryRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        documentUpdateService.moveDocumentToDirectory(request.getDocumentIds(), memberId, request.getDirectoryId());
    }

    @Operation(summary = "문서 내용 업데이트")
    @PatchMapping(value = "/documents/{document_id}/update-content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiErrorCodeExamples({DOCUMENT_NOT_FOUND, AMAZON_SERVICE_EXCEPTION})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeDocumentContent(
            @PathVariable(name = "document_id") Long documentId,
            @Valid @ModelAttribute UpdateDocumentContentRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        documentUpdateService.updateDocumentContent(request.getFile(), documentId, memberId, request.getName());
    }

    @Operation(summary = "문서 이름 변경")
    @PatchMapping("/documents/{document_id}/update-name")
    @ApiErrorCodeExample(DOCUMENT_NOT_FOUND)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateDocumentName(
            @PathVariable(name = "document_id") Long documentId,
            @Valid @RequestBody UpdateDocumentNameRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        documentUpdateService.updateDocumentName(documentId, memberId, request.getName());
    }

    @Operation(summary = "오늘의 퀴즈 관리(문제를 가져올 노트 선택)", description = "Request map에서 key값은 number, value값은 boolean입니다.")
    @PatchMapping("/documents/today-quiz-settings")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void selectDocumentToNotGenerateByTodayQuiz(
            @Valid @RequestBody UpdateTodayQuizSettingsRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        documentUpdateService.selectDocumentToNotGenerateByTodayQuiz(request.getDocumentIdTodayQuizMap(), memberId);
    }

    /**
     * DELETE
     */

    @Operation(summary = "문서 삭제")
    @DeleteMapping("/documents/delete-documents")
    @ApiErrorCodeExample(DOCUMENT_NOT_FOUND)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(
            @Valid @RequestBody DeleteDocumentRequest request
            ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        documentDeleteService.deleteDocument(memberId, request.getDocumentIds());
    }
}
