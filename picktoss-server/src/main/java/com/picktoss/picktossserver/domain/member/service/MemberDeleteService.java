package com.picktoss.picktossserver.domain.member.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.entity.WithdrawalReason;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.member.repository.WithdrawalReasonRepository;
import com.picktoss.picktossserver.global.enums.member.WithdrawalReasonContent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.MEMBER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberDeleteService {

    private final MemberRepository memberRepository;
    private final WithdrawalReasonRepository withdrawalReasonRepository;

    @Transactional
    public void deleteMember(Long memberId, String detail, WithdrawalReasonContent reason) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        WithdrawalReason withdrawalReason = WithdrawalReason.createWithdrawalReason(reason, detail);

        withdrawalReasonRepository.save(withdrawalReason);
        memberRepository.delete(member);
    }
}
