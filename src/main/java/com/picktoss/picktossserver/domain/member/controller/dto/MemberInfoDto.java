package com.picktoss.picktossserver.domain.member.controller.dto;

import com.picktoss.picktossserver.domain.member.entity.Member;
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
                .googleClientId(sub)
                .name(name)
                .email(email)
                .build();
    }
}
