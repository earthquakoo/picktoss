package com.picktoss.picktossserver.domain.publicquizcollection.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.publicquizcollection.entity.PublicQuizCollection;
import com.picktoss.picktossserver.domain.publicquizcollection.entity.PublicQuizCollectionBookmark;
import com.picktoss.picktossserver.domain.publicquizcollection.repository.PublicQuizCollectionBookmarkRepository;
import com.picktoss.picktossserver.domain.publicquizcollection.repository.PublicQuizCollectionRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicQuizCollectionBookmarkDeleteService {

    private final MemberRepository memberRepository;
    private final PublicQuizCollectionRepository publicQuizCollectionRepository;
    private final PublicQuizCollectionBookmarkRepository publicQuizCollectionBookmarkRepository;

    @Transactional
    public void deletePublicQuizCollectionBookmark(Long memberId, Long publicQuizCollectionId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorInfo.MEMBER_NOT_FOUND));

        PublicQuizCollection publicQuizCollection = publicQuizCollectionRepository.findById(publicQuizCollectionId)
                .orElseThrow(() -> new CustomException(ErrorInfo.PUBLIC_QUIZ_COLLECTION_NOT_FOUND));

        PublicQuizCollectionBookmark publicQuizCollectionBookmark = publicQuizCollectionBookmarkRepository.findByMemberAndPublicQuizCollection(member, publicQuizCollection)
                .orElseThrow(() -> new CustomException(ErrorInfo.PUBLIC_QUIZ_COLLECTION_BOOKMARK_NOT_FOUND));

        publicQuizCollectionBookmarkRepository.delete(publicQuizCollectionBookmark);
    }
}
