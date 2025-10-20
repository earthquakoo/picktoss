package com.picktoss.picktossserver.global.utils;

import com.picktoss.picktossserver.domain.document.entity.DocumentBookmark;
import com.picktoss.picktossserver.domain.document.repository.DocumentBookmarkRepository;

import java.util.List;

@FunctionalInterface
public interface DocumentBookmarkFetchFunction {
    List<DocumentBookmark> fetch(DocumentBookmarkRepository repository, Long memberId, String language);
}
