package com.picktoss.picktossserver.domain.keypoint.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.keypoint.controller.response.GetAllDocumentKeyPointsResponse;
import com.picktoss.picktossserver.domain.keypoint.entity.KeyPoint;
import com.picktoss.picktossserver.domain.keypoint.repository.KeyPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.KEY_POINT_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KeyPointService {

    private final KeyPointRepository keyPointRepository;

    public List<GetAllDocumentKeyPointsResponse.GetAllDocumentDto> findAllCategoryKeyPoints(List<Document> documents) {
        List<GetAllDocumentKeyPointsResponse.GetAllDocumentDto> documentDtos = new ArrayList<>();

        for (Document document : documents) {
            List<KeyPoint> keyPoints = document.getKeyPoints();
            List<GetAllDocumentKeyPointsResponse.GetAllKeyPointDto> keyPointDtos = new ArrayList<>();
            for (KeyPoint keyPoint : keyPoints) {
                GetAllDocumentKeyPointsResponse.GetAllKeyPointDto keyPointDto = GetAllDocumentKeyPointsResponse.GetAllKeyPointDto.builder()
                        .id(keyPoint.getId())
                        .question(keyPoint.getQuestion())
                        .answer(keyPoint.getAnswer())
                        .build();

                keyPointDtos.add(keyPointDto);
            }

            GetAllDocumentKeyPointsResponse.GetAllDocumentDto documentDto = GetAllDocumentKeyPointsResponse.GetAllDocumentDto.builder()
                    .id(document.getId())
                    .documentName(document.getName())
                    .status(document.getStatus())
                    .createdAt(document.getCreatedAt())
                    .keyPoints(keyPointDtos)
                    .build();

            documentDtos.add(documentDto);
        }
        return documentDtos;
    }

    public List<KeyPoint> findKeyPoints(Long documentId, Long memberId) {
        return keyPointRepository.findAllByDocumentIdAndMemberId(documentId, memberId);
    }

    public List<KeyPoint> findBookmarkedKeyPoint(Long memberId) {
        return keyPointRepository.findByBookmark(memberId);
    }

    public List<KeyPoint> findKeypointSearchResult(String word, Long memberId) {
        List<KeyPoint> keyPoints = keyPointRepository.findByBookmark(memberId);
        List<KeyPoint> keyPointList = new ArrayList<>();
        for (KeyPoint keyPoint : keyPoints) {
            String question = keyPoint.getQuestion();
            String answer = keyPoint.getAnswer();

            Document document = keyPoint.getDocument();
            String documentName = document.getName();

            if (question.toLowerCase().contains(word.toLowerCase())
                    || answer.toLowerCase().contains(word.toLowerCase())
                    || documentName.toLowerCase().contains(word.toLowerCase())) {
                keyPointList.add(keyPoint);
            }
        }
        return keyPointList;
    }

    @Transactional
    public void updateBookmarkKeyPoint(Long keyPointId, boolean bookmark) {
        Optional<KeyPoint> optionalKeyPoint = keyPointRepository.findById(keyPointId);

        if (optionalKeyPoint.isEmpty()) {
            throw new CustomException(KEY_POINT_NOT_FOUND);
        }

        KeyPoint keyPoint = optionalKeyPoint.get();
        keyPoint.updateBookmark(bookmark);
    }
}
