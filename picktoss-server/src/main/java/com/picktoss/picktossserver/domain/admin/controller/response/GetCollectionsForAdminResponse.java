package com.picktoss.picktossserver.domain.admin.controller.response;

import com.picktoss.picktossserver.global.enums.collection.CollectionCategory;
import com.picktoss.picktossserver.global.enums.member.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetCollectionsForAdminResponse {

    private List<GetCollectionsForAdminCollectionDto> collections;

    @Getter
    @Builder
    public static class GetCollectionsForAdminCollectionDto {
        private Long id;
        private CollectionCategory collectionCategory;
        private String name;
        private int quizCount;
        private int bookmarkCount;
        private MemberRole memberRole;
        private String memberName;
        private String description;
        private int complaintCount;
        private Boolean isDeleted;
    }
}
