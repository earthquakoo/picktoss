package com.picktoss.picktossserver.domain.admin.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.repository.CollectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCollectionUpdateService {

    private final CollectionRepository collectionRepository;

    @Transactional
    public void updateCollectionVisibility(Long collectionId, Boolean isPublic) {
        Collection collection = collectionRepository.findCollectionById(collectionId)
                .orElseThrow(() -> new CustomException(ErrorInfo.COLLECTION_NOT_FOUND));

        collection.updateCollectionByIsDeleted(isPublic);
    }
}
