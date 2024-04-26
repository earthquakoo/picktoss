package com.picktoss.picktossserver.domain.keypoint.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.keypoint.controller.response.GetAllDocumentKeyPointsResponse;
import com.picktoss.picktossserver.domain.keypoint.controller.response.GetKeyPointSetResponse;
import com.picktoss.picktossserver.domain.keypoint.entity.KeyPoint;
import com.picktoss.picktossserver.domain.keypoint.entity.KeyPointKeyPointSet;
import com.picktoss.picktossserver.domain.keypoint.entity.KeyPointSet;
import com.picktoss.picktossserver.domain.keypoint.repository.KeyPointSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.QUIZ_SET_NOT_FOUND_ERROR;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KeyPointService {

    private final KeyPointSetRepository keyPointSetRepository;

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

    public List<GetKeyPointSetResponse.GetKeyPointDto> findKeyPointSet(String questionSetId) {
        Optional<KeyPointSet> keyPointSet = keyPointSetRepository.findById(questionSetId);
        if (keyPointSet.isEmpty()) {
            throw new CustomException(QUIZ_SET_NOT_FOUND_ERROR);
        }
        List<KeyPointKeyPointSet> keyPointKeyPointSets = keyPointSet.get().getKeyPointKeyPointSets();
        List<GetKeyPointSetResponse.GetKeyPointDto> keyPointDtos = new ArrayList<>();

        for (KeyPointKeyPointSet kks : keyPointKeyPointSets) {
            KeyPoint keyPoint = kks.getKeyPoint();
            Document document = keyPoint.getDocument();
            Category category = document.getCategory();

            GetKeyPointSetResponse.GetCategoryDto categoryDto = GetKeyPointSetResponse.GetCategoryDto.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .build();

            GetKeyPointSetResponse.GetDocumentDto documentDto = GetKeyPointSetResponse.GetDocumentDto.builder()
                    .id(document.getId())
                    .name(document.getName())
                    .build();

            GetKeyPointSetResponse.GetKeyPointDto questionDto = GetKeyPointSetResponse.GetKeyPointDto.builder()
                    .id(keyPoint.getId())
                    .question(keyPoint.getQuestion())
                    .answer(keyPoint.getAnswer())
                    .document(documentDto)
                    .category(categoryDto)
                    .build();

            keyPointDtos.add(questionDto);
        }
        return keyPointDtos;
    }

//    public GetKeyPointsSetTodayResponse findQuestionSetToday(Long memberId, List<Document> documents) {
//        if (documents.isEmpty()) {
//            return GetKeyPointsSetTodayResponse.builder()
//                    .message("Document not create yet.")
//                    .build();
//        }
//        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
//        LocalDateTime todayStartTime = LocalDateTime.of(now.toLocalDate(), LocalTime.MIN);
//        LocalDateTime todayEndTime = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);
//        List<KeyPointSet> keyPointSets = keyPointSetRepository.findAllByMemberId(memberId);
//        List<KeyPointSet> todayQuestionSets = new ArrayList<>();
//        for (KeyPointSet kps : keyPointSets) {
//            if (kps.getCreatedAt().isAfter(todayStartTime) && kps.getCreatedAt().isBefore(todayEndTime)) {
//                todayQuestionSets.add(kps);
//            }
//        }
//
//        if (todayQuestionSets.isEmpty()) {
//            return GetKeyPointsSetTodayResponse.builder()
//                    .message("Question set not ready.")
//                    .build();
//        }
//
//        KeyPointSet todayQuestionSet = todayQuestionSets.stream()
//                .sorted(Comparator.comparing(KeyPointSet::getCreatedAt).reversed())
//                .toList()
//                .getFirst();
//
//        return GetKeyPointsSetTodayResponse.builder()
//                .questionSetId(todayQuestionSet.getId())
//                .build();
//    }
}
