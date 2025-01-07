package com.picktoss.picktossserver.domain.collection.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.COLLECTION_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionDeleteService {

    private final CollectionRepository collectionRepository;

    @Transactional
    public void deleteCollection(Long collectionId, Long memberId) {
        Collection collection = collectionRepository.findCollectionByCollectionIdAndMemberId(collectionId, memberId)
                .orElseThrow(() -> new CustomException(COLLECTION_NOT_FOUND));

        collectionRepository.delete(collection);
    }
}
