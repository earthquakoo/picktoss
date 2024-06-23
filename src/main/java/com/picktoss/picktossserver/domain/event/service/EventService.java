package com.picktoss.picktossserver.domain.event.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.event.constant.EventConstant;
import com.picktoss.picktossserver.domain.event.entity.Event;
import com.picktoss.picktossserver.domain.event.repository.EventRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.EVENT_NOT_FOUND;
import static com.picktoss.picktossserver.domain.event.constant.EventConstant.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    @Transactional
    public void createEvent(Member member) {
        Optional<Event> optionalEvent = eventRepository.findByMemberId(member.getId());

        if (optionalEvent.isEmpty()) {
            Event event = Event.createEvent(
                    FIRST_LOGIN_POINT, member, LocalDateTime.now());
            eventRepository.save(event);
        }
    }

    @Transactional
    public Integer checkContinuousQuizSolvedDate(Long memberId) {
        Event event = eventRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(EVENT_NOT_FOUND));

        LocalDate now = LocalDate.now();
        LocalDateTime midnight = LocalDateTime.of(now, LocalTime.MIDNIGHT);
        if (LocalDateTime.now().isAfter(midnight) && event.getUpdatedAt().isBefore(midnight)) {
            LocalDate lastUpdatedDate = event.getUpdatedAt().toLocalDate();

            if (!lastUpdatedDate.plusDays(1).equals(now)) {
                event.initContinuousSolvedQuizDateCount();
                event.changeUpdateAtByCurrentTime();
            }

            event.addContinuousSolvedQuizDateCount();
            event.changeUpdateAtByCurrentTime();

            if (event.getContinuousSolvedQuizDateCount() >= event.getMaxContinuousSolvedQuizDateCount()) {
                event.updateMaxContinuousSolvedQuizDateCount(event.getContinuousSolvedQuizDateCount());
            }

            if ((event.getContinuousSolvedQuizDateCount() % 5) == 0) {
                event.addPointBySolvingTodayQuizFiveContinuousDays();
                return FIVE_DAYS_CONTINUOUS_POINT;
            } else {
                event.addPointBySolvingTodayQuizOneContinuousDays();
                return ONE_DAYS_POINT;
            }
        }
        return null;
    }

    public Event findEventByMemberId(Long memberId) {
        return eventRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException((EVENT_NOT_FOUND)));
    }

    // 클라이언트 테스트 전용 API(실제 서비스 사용 X)
    @Transactional
    public void changePointForTest(Long memberId, int point) {
        Event event = eventRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException((EVENT_NOT_FOUND)));

        event.changePointForTest(point);
    }
}
