package com.picktoss.picktossserver.domain.feedback.service;

import com.picktoss.picktossserver.domain.feedback.entity.Feedback;
import com.picktoss.picktossserver.domain.feedback.repository.FeedbackRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.enums.feedback.FeedbackType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    @Transactional
    public void createFeedback(String title, String content, FeedbackType type, Member member) {
        Feedback feedback = Feedback.createFeedback(title, content, type, member);
        feedbackRepository.save(feedback);
    }
}
