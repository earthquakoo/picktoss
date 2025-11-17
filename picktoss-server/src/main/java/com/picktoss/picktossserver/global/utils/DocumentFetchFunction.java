package com.picktoss.picktossserver.global.utils;

import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;

import java.util.List;

@FunctionalInterface
public interface DocumentFetchFunction{
    List<Document> fetch(DocumentRepository repository, Long memberId);
}
