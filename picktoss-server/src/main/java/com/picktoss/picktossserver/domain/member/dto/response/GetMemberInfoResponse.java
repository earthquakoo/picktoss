package com.picktoss.picktossserver.domain.member.dto.response;

import com.picktoss.picktossserver.global.enums.member.SocialPlatform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GetMemberInfoResponse {

    private Long id;
    private String name;
    private String email;
    private CategoryDto category;
    private SocialPlatform socialPlatform;
    private boolean isQuizNotificationEnabled;
    private int star;
    private int bookmarkCount;
    private int totalQuizCount;
    private int monthlySolvedQuizCount;

    @Getter
    @Builder
    public static class CategoryDto {
        private Long id;
        private String name;
        private String emoji;
    }
}
