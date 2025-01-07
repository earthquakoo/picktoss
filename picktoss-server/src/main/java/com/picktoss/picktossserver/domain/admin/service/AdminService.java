package com.picktoss.picktossserver.domain.admin.service;

import com.picktoss.picktossserver.domain.admin.controller.response.GetCollectionsForAdminResponse;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.repository.CollectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final CollectionRepository collectionRepository;

    public GetCollectionsForAdminResponse findCollections() {
        List<Collection> collections = collectionRepository.findAllWithAdminPrivileges();

        List<GetCollectionsForAdminResponse.GetCollectionsForAdminCollectionDto> collectionDtos = new ArrayList<>();

        for (Collection collection : collections) {
            GetCollectionsForAdminResponse.GetCollectionsForAdminCollectionDto collectionDto = GetCollectionsForAdminResponse.GetCollectionsForAdminCollectionDto.builder()
                    .id(collection.getId())
                    .name(collection.getName())
                    .collectionCategory(collection.getCollectionCategory())
                    .bookmarkCount(collection.getCollectionBookmarks().size())
                    .complaintCount(collection.getCollectionComplaints().size())
                    .quizCount(collection.getCollectionQuizzes().size())
                    .memberRole(collection.getMember().getRole())
                    .memberName(collection.getMember().getName())
                    .build();

            collectionDtos.add(collectionDto);
        }

        return new GetCollectionsForAdminResponse(collectionDtos);
    }
}
