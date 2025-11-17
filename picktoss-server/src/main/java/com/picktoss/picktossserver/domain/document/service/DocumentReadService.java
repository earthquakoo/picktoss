package com.picktoss.picktossserver.domain.document.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.core.s3.S3Provider;
import com.picktoss.picktossserver.domain.document.dto.response.GetAllDocumentsResponse;
import com.picktoss.picktossserver.domain.document.dto.response.GetBookmarkedDocumentsResponse;
import com.picktoss.picktossserver.domain.document.dto.response.GetIsNotPublicDocumentsResponse;
import com.picktoss.picktossserver.domain.document.dto.response.GetSingleDocumentResponse;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.entity.DocumentBookmark;
import com.picktoss.picktossserver.domain.document.repository.DocumentBookmarkRepository;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.quiz.entity.DailyQuizRecordDetail;
import com.picktoss.picktossserver.domain.quiz.entity.Option;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import com.picktoss.picktossserver.domain.quiz.repository.DailyQuizRecordDetailRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetQuizRepository;
import com.picktoss.picktossserver.global.enums.document.BookmarkedDocumentSortOption;
import com.picktoss.picktossserver.global.enums.document.DocumentSortOption;
import com.picktoss.picktossserver.global.enums.document.QuizGenerationStatus;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentReadService {

    private final S3Provider s3Provider;
    private final DocumentRepository documentRepository;
    private final DocumentBookmarkRepository documentBookmarkRepository;
    private final QuizSetQuizRepository quizSetQuizRepository;
    private final DailyQuizRecordDetailRepository dailyQuizRecordDetailRepository;


    public GetSingleDocumentResponse findSingleDocument(Long memberId, Long documentId, String language) {
        Document document = documentRepository.findDocumentByDocumentIdAndMemberIdAndLanguage(documentId, memberId, language)
                .orElseThrow(() -> new CustomException(ErrorInfo.DOCUMENT_NOT_FOUND));

        String content = s3Provider.findFile(document.getS3Key());
        int characterCount = content.length();

        boolean isOwner = false;
        Member member = document.getDirectory().getMember();
        if (Objects.equals(memberId, member.getId())) {
            isOwner = true;
        }

        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        List<Quiz> quizzes = new ArrayList<>(document.getQuizzes());
        quizzes.sort(Comparator.comparing(Quiz::getId).reversed());

        List<GetSingleDocumentResponse.GetSingleDocumentReviewNeededDto> reviewNeedEdQuizzes = new ArrayList<>();
        List<GetSingleDocumentResponse.GetSingleDocumentQuizDto> quizDtos = new ArrayList<>();
        for (Quiz quiz : quizzes) {
            List<String> optionList = new ArrayList<>();
            if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                List<String> options = quiz.getOptions().stream()
                        .sorted(Comparator.comparing(Option::getId))
                        .map(Option::getOption)
                        .toList();
                if (options.isEmpty()) {
                    continue;
                }
                optionList.addAll(options);
            }

            GetSingleDocumentResponse.GetSingleDocumentQuizDto quizDto = GetSingleDocumentResponse.GetSingleDocumentQuizDto.builder()
                    .id(quiz.getId())
                    .question(quiz.getQuestion())
                    .answer(quiz.getAnswer())
                    .explanation(quiz.getExplanation())
                    .options(optionList)
                    .quizType(quiz.getQuizType())
                    .build();

            quizDtos.add(quizDto);
        }

        if (memberId != null) {
            List<QuizSetQuiz> quizSetQuizzes = quizSetQuizRepository.findAllByMemberIdAndDocumentIdAndCreatedAtAfterAndSolvedTrue(memberId, documentId, oneMonthAgo);
            for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {
                if (!quizSetQuiz.getIsAnswer()) {
                    String choseAnswer = quizSetQuiz.getChoseAnswer();
                    Quiz quiz = quizSetQuiz.getQuiz();
                    List<String> optionList = new ArrayList<>();
                    if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                        List<String> options = quiz.getOptions().stream()
                                .sorted(Comparator.comparing(Option::getId))
                                .map(Option::getOption)
                                .toList();
                        if (options.isEmpty()) {
                            continue;
                        }
                        optionList.addAll(options);
                    }

                    GetSingleDocumentResponse.GetSingleDocumentReviewNeededDto reviewNeededDto = GetSingleDocumentResponse.GetSingleDocumentReviewNeededDto.builder()
                            .id(quiz.getId())
                            .question(quiz.getQuestion())
                            .answer(quiz.getAnswer())
                            .choseAnswer(choseAnswer)
                            .explanation(quiz.getExplanation())
                            .options(optionList)
                            .quizType(quiz.getQuizType())
                            .build();

                    reviewNeedEdQuizzes.add(reviewNeededDto);
                }
            }

            List<DailyQuizRecordDetail> dailyQuizRecordDetails = dailyQuizRecordDetailRepository.findAllByMemberIdAndDocumentIdAndSolvedDateAfter(memberId, documentId, oneMonthAgo);
            for (DailyQuizRecordDetail dailyQuizRecordDetail : dailyQuizRecordDetails) {
                if (!dailyQuizRecordDetail.getIsAnswer()) {
                    Quiz quiz = dailyQuizRecordDetail.getQuiz();
                    String choseAnswer = dailyQuizRecordDetail.getChoseAnswer();

                    List<String> optionList = new ArrayList<>();
                    if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                        List<String> options = quiz.getOptions().stream()
                                .sorted(Comparator.comparing(Option::getId))
                                .map(Option::getOption)
                                .toList();
                        if (options.isEmpty()) {
                            continue;
                        }
                        optionList.addAll(options);
                    }

                    GetSingleDocumentResponse.GetSingleDocumentReviewNeededDto reviewNeededDto = GetSingleDocumentResponse.GetSingleDocumentReviewNeededDto.builder()
                            .id(quiz.getId())
                            .question(quiz.getQuestion())
                            .answer(quiz.getAnswer())
                            .choseAnswer(choseAnswer)
                            .explanation(quiz.getExplanation())
                            .options(optionList)
                            .quizType(quiz.getQuizType())
                            .build();

                    reviewNeedEdQuizzes.add(reviewNeededDto);
                }
            }
        }

        int bookmarkCount = 0;
        boolean isBookmarked = false;

        if (Boolean.TRUE.equals(document.getIsPublic())) {
            Set<DocumentBookmark> documentBookmarks = document.getDocumentBookmarks();

            if (documentBookmarks != null && !documentBookmarks.isEmpty()) {
                bookmarkCount = documentBookmarks.size();
                isBookmarked = documentBookmarks.stream()
                        .anyMatch(bookmark -> Objects.equals(bookmark.getMember().getId(), memberId));
            }
        }

        QuizGenerationStatus quizGenerationStatus = document.updateDocumentStatusClientResponse(document.getQuizGenerationStatus());

        return GetSingleDocumentResponse.builder()
                .id(document.getId())
                .name(document.getName())
                .emoji(document.getEmoji())
                .content(content)
                .creator(member.getName())
                .tryCount(document.getTryCount())
                .bookmarkCount(bookmarkCount)
                .isOwner(isOwner)
                .isPublic(document.getIsPublic())
                .isBookmarked(isBookmarked)
                .characterCount(characterCount)
                .totalQuizCount(quizDtos.size())
                .createdAt(document.getCreatedAt())
                .documentType(document.getDocumentType())
                .category(document.getCategory().getName())
                .quizGenerationStatus(quizGenerationStatus)
                .quizzes(quizDtos)
                .reviewNeededQuizzes(reviewNeedEdQuizzes)
                .build();
    }

    //모든 문서 가져오기
    public GetAllDocumentsResponse findAllDocuments(Long memberId, DocumentSortOption documentSortOption) {
        DocumentSortOption option = (documentSortOption == null)
                ? DocumentSortOption.WRONG_ANSWER_COUNT
                : documentSortOption;

        List<Document> documents = option.fetchDocuments(documentRepository, memberId);

        List<GetAllDocumentsResponse.GetAllDocumentsDocumentDto> documentDtos = new ArrayList<>();
        for (Document document : documents) {
            int reviewNeededQuizCount = 0;
            List<Quiz> quizzes = new ArrayList<>(document.getQuizzes());
            quizzes.sort(Comparator.comparing(Quiz::getId).reversed());
            for (Quiz quiz : quizzes) {
                boolean reviewNeeded = quiz.isReviewNeeded();
                if (reviewNeeded) {
                    reviewNeededQuizCount += 1;
                }
            }

            String previewContent = "";

            if (!quizzes.isEmpty()) {
                previewContent = quizzes.getFirst().getQuestion();
            }

            int bookmarkCount = 0;

            if (document.getIsPublic()) {
                Set<DocumentBookmark> documentBookmarks = document.getDocumentBookmarks();
                if (documentBookmarks != null && !documentBookmarks.isEmpty()) {
                    bookmarkCount = documentBookmarks.size();
                }
            }

            GetAllDocumentsResponse.GetAllDocumentsDocumentDto documentDto = GetAllDocumentsResponse.GetAllDocumentsDocumentDto.builder()
                    .id(document.getId())
                    .name(document.getName())
                    .isPublic(document.getIsPublic())
                    .emoji(document.getEmoji())
                    .tryCount(document.getTryCount())
                    .bookmarkCount(bookmarkCount)
                    .previewContent(previewContent)
                    .totalQuizCount(quizzes.size())
                    .reviewNeededQuizCount(reviewNeededQuizCount)
                    .build();

            documentDtos.add(documentDto);
        }

        return new GetAllDocumentsResponse(documentDtos);
    }

    //북마크된 문서 가져오기
    public GetBookmarkedDocumentsResponse findBookmarkedDocuments(Long memberId, BookmarkedDocumentSortOption documentSortOption) {
        BookmarkedDocumentSortOption option = (documentSortOption == null)
                ? BookmarkedDocumentSortOption.NAME
                : documentSortOption;

        List<DocumentBookmark> documentBookmarks = option.fetchDocuments(documentBookmarkRepository, memberId);

        List<GetBookmarkedDocumentsResponse.GetBookmarkedDocumentsDto> documentsDtos = new ArrayList<>();
        for (DocumentBookmark documentBookmark : documentBookmarks) {
            Document document = documentBookmark.getDocument();

            Set<Quiz> quizzes = document.getQuizzes();
            List<Quiz> quizList = new ArrayList<>(quizzes);
            Quiz quiz = quizList.getFirst();
            String question = quiz.getQuestion();

            GetBookmarkedDocumentsResponse.GetBookmarkedDocumentsDto documentsDto = GetBookmarkedDocumentsResponse.GetBookmarkedDocumentsDto.builder()
                    .id(document.getId())
                    .name(document.getName())
                    .emoji(document.getEmoji())
                    .previewContent(question)
                    .tryCount(document.getTryCount())
                    .bookmarkCount(document.getDocumentBookmarks().size())
                    .totalQuizCount(document.getQuizzes().size())
                    .build();

            documentsDtos.add(documentsDto);
        }

        return new GetBookmarkedDocumentsResponse(documentsDtos);
    }

    public GetIsNotPublicDocumentsResponse findIsNotPublicDocuments(Long memberId) {
        List<Document> documents = documentRepository.findAllByIsNotPublicAndMemberId(memberId);

        List<GetIsNotPublicDocumentsResponse.GetIsNotPublicDocuments> documentDtos = new ArrayList<>();
        for (Document document : documents) {
            Set<Quiz> quizzes = document.getQuizzes();
            List<Quiz> quizList = new ArrayList<>(quizzes);
            String question = "";
            int totalQuizCount = 0;
            if (!quizList.isEmpty()) {
                totalQuizCount = quizzes.size();
                question = quizList.getFirst().getQuestion();
            }

            GetIsNotPublicDocumentsResponse.GetIsNotPublicDocuments documentDto = GetIsNotPublicDocumentsResponse.GetIsNotPublicDocuments.builder()
                    .id(document.getId())
                    .name(document.getName())
                    .emoji(document.getEmoji())
                    .previewContent(question)
                    .isPublic(false)
                    .totalQuizCount(totalQuizCount)
                    .build();

            documentDtos.add(documentDto);
        }

        return new GetIsNotPublicDocumentsResponse(documentDtos);
    }
}
