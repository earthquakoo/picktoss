package com.picktoss.picktossserver.domain.keypoint.facade;


import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.service.DocumentService;
import com.picktoss.picktossserver.domain.keypoint.controller.response.GetAllDocumentKeyPointsResponse;
import com.picktoss.picktossserver.domain.keypoint.controller.response.GetBookmarkedKeyPointResponse;
import com.picktoss.picktossserver.domain.keypoint.entity.KeyPoint;
import com.picktoss.picktossserver.domain.keypoint.service.KeyPointService;
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

    public List<GetBookmarkedKeyPointResponse.GetBookmarkedKeyPointDto> findBookmarkedKeyPoint() {
        return keyPointService.findBookmarkedKeyPoint();
    }

    @Transactional
    public void updateBookmarkKeypoint(Long keyPointId, boolean bookmark) {
        keyPointService.updateBookmarkKeyPoint(keyPointId, bookmark);
    }
}
