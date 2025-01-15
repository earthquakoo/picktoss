package com.picktoss.picktossserver.domain.member.dto.request;

import com.picktoss.picktossserver.global.enums.member.WithdrawalReasonContent;
import lombok.Getter;

@Getter
public class DeleteMemberRequest {
    private WithdrawalReasonContent reason;
    private String detail;
}
