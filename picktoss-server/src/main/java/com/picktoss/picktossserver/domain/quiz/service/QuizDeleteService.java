package com.picktoss.picktossserver.domain.quiz.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.QUIZ_NOT_FOUND_ERROR;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizDeleteService {

    private final QuizRepository quizRepository;

    @Transactional
    public void deleteQuiz(Long quizId, Long memberId) {
        Quiz quiz = quizRepository.findByQuizIdAndMemberId(quizId, memberId)
                .orElseThrow(() -> new CustomException(QUIZ_NOT_FOUND_ERROR));

        Document document = quiz.getDocument();
        if (document.getQuizzes().size() - 1 <= 5) {
            document.updateDocumentIsPublic(false);
        }

        quizRepository.delete(quiz);
    }
}
