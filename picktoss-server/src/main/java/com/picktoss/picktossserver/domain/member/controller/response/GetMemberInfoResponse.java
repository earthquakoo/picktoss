package com.picktoss.picktossserver.domain.member.controller.response;

import com.picktoss.picktossserver.global.enums.member.MemberRole;
import com.picktoss.picktossserver.global.enums.member.SocialPlatform;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GetMemberInfoResponse {

    private Long id;
    private String name;
    private String email;
    private SocialPlatform socialPlatform;
    private MemberRole role;
    private List<String> interestCategories;
    private GetMemberInfoDocumentDto documentUsage;
    private boolean isQuizNotificationEnabled;
    private int star;


    @Getter
    @Builder
    public static class GetMemberInfoDocumentDto {
        // 보유한 문서 개수
        private int possessDocumentCount;
        // Free 플랜 최대 문서 보유 개수 40
        private int maxPossessDocumentCount;
    }
}
