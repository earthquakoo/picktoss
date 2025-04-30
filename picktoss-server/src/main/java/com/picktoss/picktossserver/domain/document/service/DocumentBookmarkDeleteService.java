package com.picktoss.picktossserver.domain.document.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.entity.DocumentBookmark;
import com.picktoss.picktossserver.domain.document.repository.DocumentBookmarkRepository;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentBookmarkDeleteService {

    private final MemberRepository memberRepository;
    private final DocumentRepository documentRepository;
    private final DocumentBookmarkRepository documentBookmarkRepository;

    @Transactional
    public void deleteDocumentBookmark(Long memberId, Long documentId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorInfo.MEMBER_NOT_FOUND));

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new CustomException(ErrorInfo.DOCUMENT_NOT_FOUND));

        DocumentBookmark documentBookmark = documentBookmarkRepository.findByDocumentAndMember(document, member)
                .orElseThrow(() -> new CustomException(ErrorInfo.DOCUMENT_BOOKMARK_NOT_FOUND));

        documentBookmarkRepository.delete(documentBookmark);
    }
}
