package com.picktoss.picktossserver.domain.member.dto.dto;

import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.enums.member.MemberRole;
import com.picktoss.picktossserver.global.enums.member.SocialPlatform;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberInfoDto {
    private String sub;
    private String email;
    private String name;

    public Member toEntity() {
        return Member.builder()
                .clientId(sub)
                .socialPlatform(SocialPlatform.GOOGLE)
                .name(name)
                .email(email)
                .role(MemberRole.ROLE_USER)
                .isQuizNotificationEnabled(true)
                .build();
    }
}
