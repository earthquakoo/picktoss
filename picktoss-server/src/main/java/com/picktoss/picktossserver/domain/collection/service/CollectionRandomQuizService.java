package com.picktoss.picktossserver.domain.collection.service;

import com.picktoss.picktossserver.domain.collection.entity.CollectionRandomQuizRecord;
import com.picktoss.picktossserver.domain.collection.repository.CollectionRandomQuizRecordRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.quiz.dto.request.UpdateRandomQuizResultRequest;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionRandomQuizService {

    private final QuizRepository quizRepository;
    private final CollectionRandomQuizRecordRepository collectionRandomQuizRecordRepository;

    @Transactional
    public void updateCollectionRandomQuizResult(List<UpdateRandomQuizResultRequest.UpdateRandomQuizResultDto> quizDtos, Long memberId) {
        List<Long> quizIds = new ArrayList<>();
        for (UpdateRandomQuizResultRequest.UpdateRandomQuizResultDto quizDto : quizDtos) {
            quizIds.add(quizDto.getId());
        }

        List<Quiz> quizzes = quizRepository.findAllByMemberIdAndQuizIds(memberId, quizIds);
        Member member = quizzes.getFirst().getDocument().getDirectory().getMember();

        CollectionRandomQuizRecord todayRandomQuizRecord = findTodayCollectionRandomQuizRecordByMemberIdAndCreatedAtBetween(member);

        Map<Long, Quiz> quizMap = quizzes.stream()
                .collect(Collectors.toMap(Quiz::getId, Function.identity()));

        for (UpdateRandomQuizResultRequest.UpdateRandomQuizResultDto quizDto : quizDtos) {
            Quiz quiz = quizMap.get(quizDto.getId());
            if (quiz != null) {
                if (quizDto.isAnswer()) {
                    quiz.updateIsReviewNeededFalseByCorrectAnswer();
                    todayRandomQuizRecord.updateQuizCountByCorrectAnswer();
                } else {
                    quiz.updateIsReviewNeededTrueByIncorrectAnswer();
                    todayRandomQuizRecord.updateQuizCountByIncorrectAnswer();
                }
            }
        }
    }

    private CollectionRandomQuizRecord findTodayCollectionRandomQuizRecordByMemberIdAndCreatedAtBetween(Member member) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        Optional<CollectionRandomQuizRecord> optionalCollectionRandomQuizRecord = collectionRandomQuizRecordRepository.findByMemberIdAndCreatedAtBetween(member.getId(), startOfDay, endOfDay);

        if (optionalCollectionRandomQuizRecord.isEmpty()) {
            CollectionRandomQuizRecord collectionRandomQuizRecord = CollectionRandomQuizRecord.createCollectionRandomQuizRecord(member);
            collectionRandomQuizRecordRepository.save(collectionRandomQuizRecord);
            return collectionRandomQuizRecord;
        } else {
            return optionalCollectionRandomQuizRecord.get();
        }
    }
}
