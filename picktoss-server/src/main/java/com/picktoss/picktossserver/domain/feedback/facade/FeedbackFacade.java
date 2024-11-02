package com.picktoss.picktossserver.domain.feedback.facade;

import com.picktoss.picktossserver.domain.feedback.service.FeedbackService;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import com.picktoss.picktossserver.global.enums.feedback.FeedbackType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackFacade {

    private final FeedbackService feedbackService;
    private final MemberService memberService;

    @Transactional
    public void createFeedback(String title, String content, FeedbackType type, Long memberId) {
        Member member = memberService.findMemberById(memberId);
        feedbackService.createFeedback(title, content, type, member);
    }
}
