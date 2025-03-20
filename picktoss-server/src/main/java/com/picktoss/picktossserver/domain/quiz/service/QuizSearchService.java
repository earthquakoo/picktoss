package com.picktoss.picktossserver.domain.quiz.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.collection.entity.CollectionQuizSet;
import com.picktoss.picktossserver.domain.collection.entity.CollectionQuizSetCollectionQuiz;
import com.picktoss.picktossserver.domain.collection.repository.CollectionQuizSetCollectionQuizRepository;
import com.picktoss.picktossserver.domain.directory.entity.Directory;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetAllQuizzesByDirectoryIdResponse;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetDocumentsNeedingReviewPickResponse;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetQuizSetResponse;
import com.picktoss.picktossserver.domain.quiz.entity.Option;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import com.picktoss.picktossserver.domain.quiz.repository.QuizRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetQuizRepository;
import com.picktoss.picktossserver.global.enums.quiz.QuizSetType;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.QUIZ_SET_TYPE_ERROR;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizSearchService {

    private final QuizRepository quizRepository;
    private final QuizSetQuizRepository quizSetQuizRepository;
    private final CollectionQuizSetCollectionQuizRepository collectionQuizSetCollectionQuizRepository;

    public GetQuizSetResponse findQuizSetByQuizSetIdAndQuizSetType(String quizSetId, QuizSetType quizSetType, Long memberId) {
        if (quizSetType == QuizSetType.COLLECTION_QUIZ_SET) {
            return findQuizSetByQuizSetIdAndCollectionQuizSetType(quizSetId, quizSetType, memberId);
        }
        List<QuizSetQuiz> quizSetQuizzes = quizSetQuizRepository.findAllByQuizSetIdAndMemberId(quizSetId, memberId);
        QuizSet quizSet = quizSetQuizzes.getFirst().getQuizSet();
        if (quizSet.getQuizSetType() != quizSetType) {
            throw new CustomException(QUIZ_SET_TYPE_ERROR);
        }

        List<GetQuizSetResponse.GetQuizSetQuizDto> quizDtos = new ArrayList<>();
        for (QuizSetQuiz quizzes : quizSetQuizzes) {
            Quiz quiz = quizzes.getQuiz();
            List<String> optionList = new ArrayList<>();
            if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                Set<Option> options = quiz.getOptions();
                for (Option option : options) {
                    optionList.add(option.getOption());
                }
            }

            Document document = quiz.getDocument();

            GetQuizSetResponse.GetQuizSetDocumentDto documentDto = GetQuizSetResponse.GetQuizSetDocumentDto.builder()
                    .id(document.getId())
                    .name(document.getName())
                    .build();


            Directory directory = document.getDirectory();

            GetQuizSetResponse.GetQuizSetDirectoryDto directoryDto = GetQuizSetResponse.GetQuizSetDirectoryDto.builder()
                    .id(directory.getId())
                    .name(directory.getName())
                    .build();

            GetQuizSetResponse.GetQuizSetQuizDto quizDto = GetQuizSetResponse.GetQuizSetQuizDto.builder()
                    .id(quiz.getId())
                    .question(quiz.getQuestion())
                    .answer(quiz.getAnswer())
                    .explanation(quiz.getExplanation())
                    .options(optionList)
                    .quizType(quiz.getQuizType())
                    .document(documentDto)
                    .directory(directoryDto)
                    .build();

            quizDtos.add(quizDto);
        }
        return new GetQuizSetResponse(quizDtos);
    }

    public GetAllQuizzesByDirectoryIdResponse findAllByMemberId(Long memberId) {
        List<Quiz> quizzes = quizRepository.findAllByMemberId(memberId);
        List<GetAllQuizzesByDirectoryIdResponse.GetAllQuizzesByDirectoryQuizDto> quizDtos = new ArrayList<>();
        Collections.shuffle(quizzes);

        for (Quiz quiz : quizzes) {
            Document document = quiz.getDocument();

            List<String> optionList = new ArrayList<>();
            if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                Set<Option> options = quiz.getOptions();
                for (Option option : options) {
                    optionList.add(option.getOption());
                }
            }

            GetAllQuizzesByDirectoryIdResponse.DocumentDto documentDto = GetAllQuizzesByDirectoryIdResponse.DocumentDto.builder()
                    .id(document.getId())
                    .name(document.getName())
                    .build();

            GetAllQuizzesByDirectoryIdResponse.GetAllQuizzesByDirectoryQuizDto quizDto = GetAllQuizzesByDirectoryIdResponse.GetAllQuizzesByDirectoryQuizDto.builder()
                    .id(quiz.getId())
                    .question(quiz.getQuestion())
                    .answer(quiz.getAnswer())
                    .explanation(quiz.getExplanation())
                    .options(optionList)
                    .quizType(quiz.getQuizType())
                    .document(documentDto)
                    .build();

            quizDtos.add(quizDto);
        }
        return new GetAllQuizzesByDirectoryIdResponse(quizDtos);
    }

    public GetAllQuizzesByDirectoryIdResponse findAllByMemberIdAndDirectoryId(Long memberId, Long directoryId) {
        List<Quiz> quizzes = quizRepository.findAllByMemberIdAndDirectoryId(memberId, directoryId);
        List<GetAllQuizzesByDirectoryIdResponse.GetAllQuizzesByDirectoryQuizDto> quizDtos = new ArrayList<>();
        Collections.shuffle(quizzes);

        for (Quiz quiz : quizzes) {
            Document document = quiz.getDocument();

            List<String> optionList = new ArrayList<>();
            if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                Set<Option> options = quiz.getOptions();
                for (Option option : options) {
                    optionList.add(option.getOption());
                }
            }

            GetAllQuizzesByDirectoryIdResponse.DocumentDto documentDto = GetAllQuizzesByDirectoryIdResponse.DocumentDto.builder()
                    .id(document.getId())
                    .name(document.getName())
                    .build();

            GetAllQuizzesByDirectoryIdResponse.GetAllQuizzesByDirectoryQuizDto quizDto = GetAllQuizzesByDirectoryIdResponse.GetAllQuizzesByDirectoryQuizDto.builder()
                    .id(quiz.getId())
                    .question(quiz.getQuestion())
                    .answer(quiz.getAnswer())
                    .explanation(quiz.getExplanation())
                    .options(optionList)
                    .quizType(quiz.getQuizType())
                    .document(documentDto)
                    .build();

            quizDtos.add(quizDto);
        }
        return new GetAllQuizzesByDirectoryIdResponse(quizDtos);
    }

    public List<Quiz> findAllGeneratedQuizzesByDocumentId(Long documentId, QuizType quizType, Long memberId) {
        if (quizType != null) {
            return quizRepository.findAllByDocumentIdAndQuizTypeAndMemberId(documentId, quizType, memberId);
        }
        return quizRepository.findAllByDocumentIdAndMemberId(documentId, memberId);
    }

    public GetDocumentsNeedingReviewPickResponse findDocumentsNeedingReviewPick(Long memberId, Long documentId) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<QuizSetQuiz> quizSetQuizzes = quizSetQuizRepository.findByMemberIdAndDocumentIdAndSolvedTrueAndCreatedAtAfter(memberId, documentId, sevenDaysAgo);

        Map<Quiz, QuizSetQuiz> quizMap = new HashMap<>();
        // 중복된 퀴즈 제거 후 map으로 변경
        for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {
            Quiz quiz = quizSetQuiz.getQuiz();
            quizMap.putIfAbsent(quiz, quizSetQuiz);
        }

        List<GetDocumentsNeedingReviewPickResponse.GetReviewQuizDto> quizDtos = new ArrayList<>();
        for (Quiz quiz : quizMap.keySet()) {
            QuizSetQuiz quizSetQuiz = quizMap.get(quiz);
            String description = "";
            if (quizSetQuiz.getElapsedTimeMs() >= 20000) {
                description = "20초 이상 소요";
            }

            if (!quizSetQuiz.getIsAnswer()) {
                description = "오답";
            }
            List<String> optionList = new ArrayList<>();
            if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                Set<Option> options = quiz.getOptions();
                if (options.isEmpty()) {
                    continue;
                }
                for (Option option : options) {
                    optionList.add(option.getOption());
                }
            }

            GetDocumentsNeedingReviewPickResponse.GetReviewQuizDto quizDto = GetDocumentsNeedingReviewPickResponse.GetReviewQuizDto.builder()
                    .id(quiz.getId())
                    .question(quiz.getQuestion())
                    .answer(quiz.getAnswer())
                    .explanation(quiz.getExplanation())
                    .options(optionList)
                    .quizType(quiz.getQuizType())
                    .description(description)
                    .choseAnswer(quizSetQuiz.getChoseAnswer())
                    .build();

            quizDtos.add(quizDto);
        }

        return new GetDocumentsNeedingReviewPickResponse(quizDtos);
    }

    public List<Quiz> findIncorrectQuizzesByMemberIdAndIsReviewNeedTrue(Long memberId) {
        return quizRepository.findAllByMemberIdAndIsReviewNeededTrue(memberId);
    }

    private GetQuizSetResponse findQuizSetByQuizSetIdAndCollectionQuizSetType(String quizSetId, QuizSetType quizSetType, Long memberId) {
        List<CollectionQuizSetCollectionQuiz> collectionQuizSetCollectionQuizzes = collectionQuizSetCollectionQuizRepository.findAllByQuizSetIdAndMemberId(quizSetId, memberId);
        CollectionQuizSet collectionQuizSet = collectionQuizSetCollectionQuizzes.getFirst().getCollectionQuizSet();
        if (collectionQuizSet.getQuizSetType() != quizSetType) {
            throw new CustomException(QUIZ_SET_TYPE_ERROR);
        }

        List<GetQuizSetResponse.GetQuizSetQuizDto> quizDtos = new ArrayList<>();
        for (CollectionQuizSetCollectionQuiz collectionQuizSetCollectionQuiz : collectionQuizSetCollectionQuizzes) {
            Quiz quiz = collectionQuizSetCollectionQuiz.getCollectionQuiz().getQuiz();

            List<String> optionList = new ArrayList<>();
            if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                Set<Option> options = quiz.getOptions();
                if (options.isEmpty()) {
                    continue;
                }
                for (Option option : options) {
                    optionList.add(option.getOption());
                }
            }

            GetQuizSetResponse.GetQuizSetQuizDto quizDto = GetQuizSetResponse.GetQuizSetQuizDto.builder()
                    .id(quiz.getId())
                    .question(quiz.getQuestion())
                    .answer(quiz.getAnswer())
                    .explanation(quiz.getExplanation())
                    .options(optionList)
                    .quizType(quiz.getQuizType())
                    .build();

            quizDtos.add(quizDto);
        }
        return new GetQuizSetResponse(quizDtos);
    }
}
