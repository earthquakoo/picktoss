package com.picktoss.picktossserver.domain.publicquizcollection.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.directory.entity.Directory;
import com.picktoss.picktossserver.domain.directory.repository.DirectoryRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.publicquizcollection.entity.PublicQuizCollection;
import com.picktoss.picktossserver.domain.publicquizcollection.entity.PublicQuizCollectionBookmark;
import com.picktoss.picktossserver.domain.publicquizcollection.repository.PublicQuizCollectionBookmarkRepository;
import com.picktoss.picktossserver.domain.publicquizcollection.repository.PublicQuizCollectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicQuizCollectionBookmarkCreateService {

    private final MemberRepository memberRepository;
    private final DirectoryRepository directoryRepository;
    private final PublicQuizCollectionRepository publicQuizCollectionRepository;
    private final PublicQuizCollectionBookmarkRepository publicQuizCollectionBookmarkRepository;

    @Transactional
    public void createPublicQuizCollectionBookmark(Long memberId, Long publicQuizCollectionId, Long directoryId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorInfo.MEMBER_NOT_FOUND));

        Directory directory = directoryRepository.findByDirectoryIdAndMemberId(directoryId, memberId)
                .orElseThrow(() -> new CustomException(ErrorInfo.DIRECTORY_NOT_FOUND));

        PublicQuizCollection publicQuizCollection = publicQuizCollectionRepository.findById(publicQuizCollectionId)
                .orElseThrow(() -> new CustomException(ErrorInfo.PUBLIC_QUIZ_COLLECTION_NOT_FOUND));

        PublicQuizCollectionBookmark publicQuizCollectionBookmark = PublicQuizCollectionBookmark.createPublicQuizCollectionBookmark(member, publicQuizCollection, directory);

        publicQuizCollectionBookmarkRepository.save(publicQuizCollectionBookmark);
    }
}
