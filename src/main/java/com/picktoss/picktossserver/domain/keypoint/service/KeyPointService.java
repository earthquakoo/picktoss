package com.picktoss.picktossserver.domain.keypoint.service;

import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.keypoint.controller.response.GetAllDocumentKeyPointsResponse;
import com.picktoss.picktossserver.domain.keypoint.entity.KeyPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KeyPointService {

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
}
