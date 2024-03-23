package com.picktoss.picktossserver.domain.document.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.s3.S3Provider;
import com.picktoss.picktossserver.core.sqs.SqsProvider;
import com.picktoss.picktossserver.domain.document.controller.response.GetAllDocumentsResponse;
import com.picktoss.picktossserver.domain.document.controller.request.CreateDocumentRequest;
import com.picktoss.picktossserver.domain.document.controller.response.CreateDocumentResponse;
import com.picktoss.picktossserver.domain.document.controller.response.GetSingleDocumentResponse;
import com.picktoss.picktossserver.domain.document.facade.DocumentFacade;
import com.picktoss.picktossserver.domain.document.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class DocumentController {

    private final S3Provider s3Provider;
    private final JwtTokenProvider jwtTokenProvider;
    private final DocumentFacade documentFacade;

    private final DocumentService documentService;

    @PostMapping("/documents")
    public ResponseEntity<CreateDocumentResponse> createDocument(CreateDocumentRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        String memberId = jwtUserInfo.getMemberId();

        String s3Key = s3Provider.uploadFile(request.getUploadFile());
        Long documentId = documentFacade.saveDocument(request.getDocumentName(), s3Key, memberId, request.getCategoryId());
        return ResponseEntity.ok().body(new CreateDocumentResponse(documentId));
    }

    @GetMapping("/categories/{category_id}/documents")
    public ResponseEntity<GetAllDocumentsResponse> getAllDocuments(@PathVariable(name = "category_id") Long categoryId) {
//        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
//        Long memberId = jwtUserInfo.getMemberId();

        List<GetAllDocumentsResponse.DocumentDto> allDocuments = documentFacade.findAllDocuments(categoryId);
        return ResponseEntity.ok().body(new GetAllDocumentsResponse(allDocuments));
    }

    @GetMapping("/documents/{document_id}")
    public ResponseEntity<GetSingleDocumentResponse> getSingleDocument(@PathVariable(name = "document_id") Long documentId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        String memberId = jwtUserInfo.getMemberId();

        List<GetSingleDocumentResponse.DocumentDto> documents = documentFacade.findSingleDocument(memberId, documentId);
        return ResponseEntity.ok().body(new GetSingleDocumentResponse(documents));
    }
}
