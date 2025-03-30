package com.picktoss.picktossserver.domain.publicquizcollection.service;

import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.publicquizcollection.entity.PublicQuizCollection;
import com.picktoss.picktossserver.domain.publicquizcollection.repository.PublicQuizCollectionBookmarkRepository;
import com.picktoss.picktossserver.domain.publicquizcollection.repository.PublicQuizCollectionRepository;
import com.picktoss.picktossserver.global.enums.document.PublicQuizCollectionCategory;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicQuizCollectionSearchService {

    private final MemberRepository memberRepository;
    private final DocumentRepository documentRepository;
    private final PublicQuizCollectionRepository publicQuizCollectionRepository;
    private final PublicQuizCollectionBookmarkRepository publicQuizCollectionBookmarkRepository;

    public List<PublicQuizCollection> findAllPublicQuizCollections(Long memberId, QuizType quizType, Integer quizCount) {
        List<PublicQuizCollection> publicQuizCollections = publicQuizCollectionRepository.findAll();

        return publicQuizCollections;

//        Member member = memberRepository.findById(memberId)
//                .orElseThrow(() -> new CustomException(ErrorInfo.MEMBER_NOT_FOUND));
//
//        List<String> interestCollectionCategories = member.getInterestCollectionCategories();

    }

    public List<PublicQuizCollection> findAllByBookmarked(Long memberId) {
        return publicQuizCollectionRepository.findAllByMemberIdAndBookmarked(memberId);
    }


    private void filterPublicQuizCollections(
            List<String> interestCollectionCategories, QuizType quizType, Integer quizCount
    ) {

        List<PublicQuizCollectionCategory> publicQuizCollectionCategories = new ArrayList<>();
        for (String interestCollectionCategory : interestCollectionCategories) {
            PublicQuizCollectionCategory publicQuizCollectionCategory = PublicQuizCollectionCategory.valueOf(interestCollectionCategory);
            publicQuizCollectionCategories.add(publicQuizCollectionCategory);
        }

        List<PublicQuizCollection> publicQuizCollections = publicQuizCollectionRepository.findAllByPublicQuizCollectionCategories(publicQuizCollectionCategories);

    }

}
