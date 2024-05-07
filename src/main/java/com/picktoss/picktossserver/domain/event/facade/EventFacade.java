package com.picktoss.picktossserver.domain.event.facade;

import com.picktoss.picktossserver.domain.event.service.EventService;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventFacade {

    private final EventService eventService;
    private final MemberService memberService;

    @Transactional
    public void attendanceCheck(Long memberId) {
        Member member = memberService.findMemberById(memberId);
        int point = eventService.attendanceCheck(member);
    }
}
