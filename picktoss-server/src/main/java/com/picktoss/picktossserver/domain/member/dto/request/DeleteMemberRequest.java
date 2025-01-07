package com.picktoss.picktossserver.domain.member.dto.request;

import lombok.Getter;

@Getter
public class DeleteMemberRequest {
    private String reason;
    private String content;
}
