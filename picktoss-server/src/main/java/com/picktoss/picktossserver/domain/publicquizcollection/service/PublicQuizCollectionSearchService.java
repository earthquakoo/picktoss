package com.picktoss.picktossserver.domain.publicquizcollection.service;

import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.publicquizcollection.repository.PublicQuizCollectionBookmarkRepository;
import com.picktoss.picktossserver.domain.publicquizcollection.repository.PublicQuizCollectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicQuizCollectionSearchService {

    private final DocumentRepository documentRepository;
    private final PublicQuizCollectionRepository publicQuizCollectionRepository;
    private final PublicQuizCollectionBookmarkRepository publicQuizCollectionBookmarkRepository;


}
