package com.picktoss.picktossserver.domain.member.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.member.dto.response.GetMemberInfoResponse;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.star.entity.Star;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.picktoss.picktossserver.domain.document.constant.DocumentConstant.FREE_PLAN_MAX_POSSESS_DOCUMENT_COUNT;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberSearchService {

    private final MemberRepository memberRepository;
    private final DocumentRepository documentRepository;

    public GetMemberInfoResponse findMemberInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorInfo.MEMBER_NOT_FOUND));

        List<Document> documents = documentRepository.findAllByMemberId(memberId);
        int possessDocumentCount = documents.size();

        Star star = member.getStar();

        GetMemberInfoResponse.GetMemberInfoDocumentDto documentDto = GetMemberInfoResponse.GetMemberInfoDocumentDto.builder()
                .possessDocumentCount(possessDocumentCount)
                .maxPossessDocumentCount(FREE_PLAN_MAX_POSSESS_DOCUMENT_COUNT)
                .build();


        String email = Optional.ofNullable(member.getEmail()).orElse("");

        return GetMemberInfoResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .email(email)
                .socialPlatform(member.getSocialPlatform())
                .interestCategories(member.getInterestCollectionCategories())
                .role(member.getRole())
                .documentUsage(documentDto)
                .star(star.getStar())
                .isQuizNotificationEnabled(member.isQuizNotificationEnabled())
                .build();
    }
}
