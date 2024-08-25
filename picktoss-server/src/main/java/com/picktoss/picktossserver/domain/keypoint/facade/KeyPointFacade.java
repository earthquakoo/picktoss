package com.picktoss.picktossserver.domain.keypoint.facade;


import com.picktoss.picktossserver.domain.document.controller.response.GetSingleDocumentResponse;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.service.DocumentService;
import com.picktoss.picktossserver.domain.keypoint.controller.response.GetAllDocumentKeyPointsResponse;
import com.picktoss.picktossserver.domain.keypoint.controller.response.GetKeyPointsResponse;
import com.picktoss.picktossserver.domain.keypoint.entity.KeyPoint;
import com.picktoss.picktossserver.domain.keypoint.service.KeyPointService;
import com.picktoss.picktossserver.global.enums.DocumentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KeyPointFacade {

    private final KeyPointService keyPointService;
    private final DocumentService documentService;

    public List<GetAllDocumentKeyPointsResponse.GetAllDocumentDto> findAllCategoryKeyPoints(Long categoryId, Long memberId) {
        List<Document> documents = documentService.findAllByCategoryIdAndMemberId(categoryId, memberId);
        return keyPointService.findAllCategoryKeyPoints(documents);
    }

    public GetKeyPointsResponse findKeyPoints(Long documentId, Long memberId) {
        Document document = documentService.findByDocumentIdAndMemberId(documentId, memberId);
        DocumentStatus documentStatus = document.updateDocumentStatusClientResponse(document.getStatus());
        return keyPointService.findKeyPoints(document, documentStatus);
    }

    public List<KeyPoint> findBookmarkedKeyPoint(Long memberId) {
        return keyPointService.findBookmarkedKeyPoint(memberId);
    }

    public List<KeyPoint> findKeypointSearchResult(String word, Long memberId) {
        return keyPointService.findKeypointSearchResult(word, memberId);
    }


        @Transactional
    public void updateBookmarkKeypoint(Long keyPointId, boolean bookmark) {
        keyPointService.updateBookmarkKeyPoint(keyPointId, bookmark);
    }
}
