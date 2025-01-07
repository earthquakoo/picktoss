package com.picktoss.picktossserver.domain.quiz.service;

import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizDownloadService {

    private final QuizRepository quizRepository;

    public List<Quiz> findAllByDocumentIdAndMemberId(Long documentId, Long memberId) {
        return quizRepository.findAllByDocumentIdAndMemberId(documentId, memberId);
    }
}
