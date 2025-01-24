package com.picktoss.picktossserver.domain.admin.service;

import com.picktoss.picktossserver.domain.admin.controller.response.GetCollectionsForAdminResponse;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.repository.CollectionRepository;
import com.picktoss.picktossserver.global.enums.collection.CollectionCategory;
import com.picktoss.picktossserver.global.enums.member.MemberRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCollectionSearchService {

    private final CollectionRepository collectionRepository;

    public GetCollectionsForAdminResponse findCollections(CollectionCategory collectionCategory, Boolean isDeleted, MemberRole memberRole, Integer quizCount) {
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
                    .isDeleted(collection.getIsDeleted())
                    .build();

            collectionDtos.add(collectionDto);
        }

        return new GetCollectionsForAdminResponse(collectionDtos);
    }

    public GetCollectionsForAdminResponse searchCollections(String keyword, String memberName) {
        List<Collection> collections = new ArrayList<>();

        if (keyword != null) {
            collections = collectionRepository.findByCollectionByKeyword(keyword);
        }

        if (memberName != null) {
            collections = collectionRepository.findByCollectionByMemberName(memberName);
        }

        List<GetCollectionsForAdminResponse.GetCollectionsForAdminCollectionDto> collectionDtos = new ArrayList<>();

        for (Collection collection : collections) {
            GetCollectionsForAdminResponse.GetCollectionsForAdminCollectionDto collectionDto = GetCollectionsForAdminResponse.GetCollectionsForAdminCollectionDto.builder()
                    .id(collection.getId())
                    .name(collection.getName())
                    .collectionCategory(collection.getCollectionCategory())
                    .bookmarkCount(collection.getCollectionBookmarks().size())
                    .complaintCount(collection.getCollectionComplaints().size())
                    .quizCount(collection.getCollectionQuizzes().size())
                    .description(collection.getDescription())
                    .memberRole(collection.getMember().getRole())
                    .memberName(collection.getMember().getName())
                    .isDeleted(collection.getIsDeleted())
                    .build();

            collectionDtos.add(collectionDto);
        }

        return new GetCollectionsForAdminResponse(collectionDtos);
    }
}
