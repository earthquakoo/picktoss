package com.picktoss.picktossserver.domain.member.dto.response;

import com.picktoss.picktossserver.domain.category.entity.Category;
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
    private Category category;
    private SocialPlatform socialPlatform;
    private boolean isQuizNotificationEnabled;
    private int star;
    private int bookmarkCount;
    private int totalQuizCount;
    private int monthlySolvedQuizCount;
}
