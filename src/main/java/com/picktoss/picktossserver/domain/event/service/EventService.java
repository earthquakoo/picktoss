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
    public int attendanceCheck(Member member) {
        Optional<Event> optionalEvent = eventRepository.findByMemberId(member.getId());

        if (optionalEvent.isEmpty()) {
            Event event = Event.createEvent(FIRST_LOGIN_POINT, 1, member);
            eventRepository.save(event);
            return event.getPoint();
        }

        Event event = optionalEvent.get();
        LocalDate now = LocalDate.now();
        LocalDateTime midnight = LocalDateTime.of(now, LocalTime.MIDNIGHT);
        if (LocalDateTime.now().isAfter(midnight) && event.getUpdatedAt().isBefore(midnight)) {
            event.addContinuousAttendanceDatesCount();
            if (event.getContinuousAttendanceDatesCount() == 5) {
                event.addPoint(FIVE_DAYS_CONTINUOUS_ATTENDANCE_POINT);
                event.initContinuousAttendanceDatesCount();
            } else {
                event.addPoint(ATTENDANCE_POINT);
            }
        }
        return event.getPoint();
    }

    public Event findEventByMemberId(Long memberId) {
        return eventRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException((EVENT_NOT_FOUND)));
    }
}
