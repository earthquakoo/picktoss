package com.picktoss.picktossserver.domain.member.facade;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtTokenDto;
import com.picktoss.picktossserver.domain.document.service.DocumentService;
import com.picktoss.picktossserver.domain.member.controller.dto.MemberInfoDto;
import com.picktoss.picktossserver.domain.member.controller.response.GetMemberInfoResponse;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import com.picktoss.picktossserver.domain.subscription.entity.Subscription;
import com.picktoss.picktossserver.domain.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberFacade {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;
    private final DocumentService documentService;
    private final SubscriptionService subscriptionService;

    @Transactional
    public JwtTokenDto createMember(MemberInfoDto memberInfoDto) {
        Optional<Member> optionalMember = memberService.findMemberByGoogleClientId(memberInfoDto.getSub());

        if (optionalMember.isEmpty()) {
            Member member = memberInfoDto.toEntity();
            memberService.createMember(member);
            subscriptionService.createSubscription(member);
            return jwtTokenProvider.generateToken(member.getId());
        }
        Long memberId = optionalMember.get().getId();
        return jwtTokenProvider.generateToken(memberId);
    }

    public GetMemberInfoResponse findMemberInfo(Long memberId) {
        Member member = memberService.findMemberById(memberId);
        Subscription subscription = subscriptionService.findCurrentSubscription(memberId, member);
        int currentSubscriptionUploadedDocumentNum = documentService.findNumUploadedDocumentsForCurrentSubscription(memberId, subscription);
        int currentUploadedDocumentNum = documentService.findNumCurrentUploadDocument(memberId);

        return memberService.findMemberInfo(memberId, subscription, currentSubscriptionUploadedDocumentNum, currentUploadedDocumentNum);
    }
}
