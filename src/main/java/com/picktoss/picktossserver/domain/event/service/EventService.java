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
                    FIRST_LOGIN_POINT, 0, 0, member);
            eventRepository.save(event);
        }
    }

    @Transactional
    public void checkContinuousQuizSolvedDate(Long memberId) {
        Optional<Event> optionalEvent = eventRepository.findByMemberId(memberId);

        if (optionalEvent.isEmpty()) {
            throw new CustomException(EVENT_NOT_FOUND);
        }

        Event event = optionalEvent.get();
        LocalDate now = LocalDate.now();
        LocalDateTime midnight = LocalDateTime.of(now, LocalTime.MIDNIGHT);
        if (LocalDateTime.now().isAfter(midnight) && event.getUpdatedAt().isBefore(midnight)) {
            LocalDate lastUpdatedDate = event.getUpdatedAt().toLocalDate();

            if (!lastUpdatedDate.plusDays(1).equals(now)) {
                event.initContinuousSolvedQuizDateCount();
            }

            event.addContinuousSolvedQuizDateCount();

            if ((event.getContinuousSolvedQuizDateCount() % 5) == 0) {
                event.addPoint(FIVE_DAYS_CONTINUOUS_POINT);
            } else {
                event.addPoint(ONE_DAYS_POINT);
            }
        }
    }

    public Event findEventByMemberId(Long memberId) {
        return eventRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException((EVENT_NOT_FOUND)));
    }
}
