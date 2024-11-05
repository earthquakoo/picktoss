package com.picktoss.picktossserver.domain.member.facade;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtTokenDto;
import com.picktoss.picktossserver.domain.directory.entity.Directory;
import com.picktoss.picktossserver.domain.directory.service.DirectoryService;
import com.picktoss.picktossserver.domain.document.service.DocumentService;
import com.picktoss.picktossserver.domain.member.controller.dto.MemberInfoDto;
import com.picktoss.picktossserver.domain.member.controller.response.GetMemberInfoResponse;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import com.picktoss.picktossserver.domain.star.entity.Star;
import com.picktoss.picktossserver.domain.star.service.StarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberFacade {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;
    private final DocumentService documentService;
    private final DirectoryService directoryService;
    private final StarService starService;

    @Transactional
    public JwtTokenDto createMember(MemberInfoDto memberInfoDto) {
        Optional<Member> optionalMember = memberService.findMemberByGoogleClientId(memberInfoDto.getSub());

        if (optionalMember.isEmpty()) {
            Member member = memberInfoDto.toEntity();
            memberService.createMember(member);
            starService.createStarBySignUp(member);
            Directory directory = directoryService.createDefaultDirectory(member);
            documentService.createDefaultDocument(directory);
            return jwtTokenProvider.generateToken(member);
        }
        Member member = optionalMember.get();
        return jwtTokenProvider.generateToken(member);
    }

    @Transactional
    public GetMemberInfoResponse findMemberInfo(Long memberId) {
        Member member = memberService.findMemberById(memberId);

        int possessDocumentCount = documentService.findPossessDocumentCount(memberId);
        Star star = member.getStar();

        return memberService.findMemberInfo(
                member,
                possessDocumentCount,
                star.getStar()
        );
    }

    @Transactional
    public void updateMemberName(Long memberId, String name) {
        memberService.updateMemberName(memberId, name);
    }

    @Transactional
    public void updateQuizNotification(Long memberId, boolean isQuizNotification) {
        memberService.updateQuizNotification(memberId, isQuizNotification);
    }

    @Transactional
    public void updateInterestCollectionFields(Long memberId, List<String> interestCollectionFields) {
        memberService.updateInterestCollectionFields(memberId, interestCollectionFields);
    }

    @Transactional
    public void updateTodayQuizCount(Long memberId, Integer todayQuizCount) {
        memberService.updateTodayQuizCount(memberId, todayQuizCount);
    }

    @Transactional
    public void createMemberForTest() {
        memberService.createMemberForTest();
    }
}
