package com.picktoss.picktossserver.domain.feedback.service;

import com.picktoss.picktossserver.domain.feedback.entity.Feedback;
import com.picktoss.picktossserver.domain.feedback.entity.FeedbackFile;
import com.picktoss.picktossserver.domain.feedback.repository.FeedbackFileRepository;
import com.picktoss.picktossserver.domain.feedback.repository.FeedbackRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.enums.feedback.FeedbackType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackFileRepository feedbackFileRepository;

    @Transactional
    public void createFeedback(String title, String content, List<String> s3Keys, FeedbackType type, String email, Member member) {
        Feedback feedback = Feedback.createFeedback(title, content, type, email, member);

        List<FeedbackFile> feedbackFiles = new ArrayList<>();
        for (String s3Key : s3Keys) {
            FeedbackFile feedbackFile = FeedbackFile.createFeedbackFile(s3Key, feedback);
            feedbackFiles.add(feedbackFile);
        }

        feedbackRepository.save(feedback);
        feedbackFileRepository.saveAll(feedbackFiles);
    }
}
