package com.picktoss.picktossserver.domain.keypoint.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.keypoint.controller.response.GetAllDocumentKeyPointsResponse;
import com.picktoss.picktossserver.domain.keypoint.controller.response.GetKeyPointsResponse;
import com.picktoss.picktossserver.domain.keypoint.entity.KeyPoint;
import com.picktoss.picktossserver.domain.keypoint.repository.KeyPointRepository;
import com.picktoss.picktossserver.global.enums.DocumentStatus;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.json.JSONParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertStoreException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.DEFAULT_FILE_NOT_FOUND;
import static com.picktoss.picktossserver.core.exception.ErrorInfo.KEY_POINT_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KeyPointService {

    private final KeyPointRepository keyPointRepository;

    private static final String defaultKeyPoints = "defaultkeypoints/default_keypoint.json";

    @Transactional
    public void createDefaultKeyPoint(Document document) {
        List<KeyPoint> keyPoints = new ArrayList<>();

        try {
            ClassPathResource classPathResource = new ClassPathResource(defaultKeyPoints);
            InputStream inputStream = classPathResource.getInputStream();

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            String jsonString = stringBuilder.toString();

            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jArray = jsonObject.getJSONArray("keyPoints");

            for (int i = 0; i < jArray.length(); i++) {
                JSONObject obj = jArray.getJSONObject(i);
                String question = obj.getString("question");
                String answer = obj.getString("answer");

                KeyPoint keyPoint = KeyPoint.builder()
                        .question(question)
                        .answer(answer)
                        .bookmark(false)
                        .document(document)
                        .build();

                keyPoints.add(keyPoint);
            }

        } catch (IOException e) {
            throw new CustomException(DEFAULT_FILE_NOT_FOUND);
        }

        keyPointRepository.saveAll(keyPoints);
    }

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

            DocumentStatus documentStatus = document.updateDocumentStatusClientResponse(document.getStatus());

            GetAllDocumentKeyPointsResponse.GetAllDocumentDto documentDto = GetAllDocumentKeyPointsResponse.GetAllDocumentDto.builder()
                    .id(document.getId())
                    .documentName(document.getName())
                    .status(documentStatus)
                    .createdAt(document.getCreatedAt())
                    .keyPoints(keyPointDtos)
                    .build();

            documentDtos.add(documentDto);
        }
        return documentDtos;
    }

    public GetKeyPointsResponse findKeyPoints(Long documentId, Long memberId, DocumentStatus documentStatus) {
        List<KeyPoint> keyPoints = keyPointRepository.findAllByDocumentIdAndMemberId(documentId, memberId);

        List<GetKeyPointsResponse.GetKeyPointsDto> keyPointsDtos = new ArrayList<>();

        for (KeyPoint keyPoint : keyPoints) {
            GetKeyPointsResponse.GetKeyPointsDto keyPointsDto = GetKeyPointsResponse.GetKeyPointsDto.builder()
                    .id(keyPoint.getId())
                    .question(keyPoint.getQuestion())
                    .answer(keyPoint.getAnswer())
                    .bookmark(keyPoint.isBookmark())
                    .updatedAt(keyPoint.getUpdatedAt())
                    .build();

            keyPointsDtos.add(keyPointsDto);
        }
        return new GetKeyPointsResponse(documentStatus, keyPointsDtos);
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
