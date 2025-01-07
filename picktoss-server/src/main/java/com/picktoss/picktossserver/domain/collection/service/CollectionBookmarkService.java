package com.picktoss.picktossserver.domain.collection.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.entity.CollectionBookmark;
import com.picktoss.picktossserver.domain.collection.repository.*;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionBookmarkService {

    private final CollectionRepository collectionRepository;
    private final CollectionBookmarkRepository collectionBookmarkRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void createCollectionBookmark(Long memberId, Long collectionId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        Collection collection = collectionRepository.findCollectionById(collectionId)
                .orElseThrow(() -> new CustomException(COLLECTION_NOT_FOUND));

        Long collectionMemberId = collection.getMember().getId();

        if (Objects.equals(memberId, collectionMemberId)) {
            throw new CustomException(OWN_COLLECTION_CANT_BOOKMARK);
        }

        Set<CollectionBookmark> collectionBookmarks = collection.getCollectionBookmarks();
        for (CollectionBookmark collectionBookmark : collectionBookmarks) {
            if (Objects.equals(collectionId, collectionBookmark.getId())) {
                throw new CustomException(COLLECTION_ALREADY_BOOKMARKED);
            }
        }

        CollectionBookmark collectionBookmark = CollectionBookmark.createCollectionBookmark(collection, member);

        collectionBookmarkRepository.save(collectionBookmark);
    }

    @Transactional
    public void deleteCollectionBookmark(Long memberId, Long collectionId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        Collection collection = collectionRepository.findCollectionById(collectionId)
                .orElseThrow(() -> new CustomException(COLLECTION_NOT_FOUND));

        CollectionBookmark collectionBookmark = collectionBookmarkRepository.findByMemberAndCollection(member, collection)
                .orElseThrow(() -> new CustomException(COLLECTION_BOOKMARK_NOT_FOUND));

        collectionBookmarkRepository.delete(collectionBookmark);
    }
}
