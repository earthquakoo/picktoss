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

    // 클라이언트 테스트 전용 API(실제 서비스 사용 X)
    @Transactional
    public void changePointForTest(Long memberId, int point) {
        eventService.changePointForTest(memberId, point);
    }
}
