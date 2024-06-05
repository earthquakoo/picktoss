package com.picktoss.picktossserver.domain.keypoint.controller.mapper;

import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.keypoint.controller.dto.KeyPointResponseDto;
import com.picktoss.picktossserver.domain.keypoint.entity.KeyPoint;

import java.util.ArrayList;
import java.util.List;

public class KeyPointMapper {

    public static KeyPointResponseDto keyPointsToKeyPointResponseDto(List<KeyPoint> keyPoints) {

        List<KeyPointResponseDto.KeyPointDto> keyPointDtos = new ArrayList<>();

        for (KeyPoint keyPoint : keyPoints) {
            Document document = keyPoint.getDocument();

            KeyPointResponseDto.DocumentDto documentDto = KeyPointResponseDto.DocumentDto.builder()
                    .id(document.getId())
                    .name(document.getName())
                    .build();

            Category category = document.getCategory();

            KeyPointResponseDto.CategoryDto categoryDto = KeyPointResponseDto.CategoryDto.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .build();

            KeyPointResponseDto.KeyPointDto keyPointDto = KeyPointResponseDto.KeyPointDto.builder()
                    .id(keyPoint.getId())
                    .question(keyPoint.getQuestion())
                    .answer(keyPoint.getAnswer())
                    .bookmark(keyPoint.isBookmark())
                    .category(categoryDto)
                    .document(documentDto)
                    .updatedAt(keyPoint.getUpdatedAt())
                    .build();

            keyPointDtos.add(keyPointDto);
        }
        return new KeyPointResponseDto(keyPointDtos);
    }
}
