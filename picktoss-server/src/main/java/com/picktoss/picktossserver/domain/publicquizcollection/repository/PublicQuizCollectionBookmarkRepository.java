package com.picktoss.picktossserver.domain.publicquizcollection.repository;

import com.picktoss.picktossserver.domain.publicquizcollection.entity.PublicQuizCollection;
import com.picktoss.picktossserver.domain.publicquizcollection.entity.PublicQuizCollectionBookmark;
import com.picktoss.picktossserver.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PublicQuizCollectionBookmarkRepository extends JpaRepository<PublicQuizCollectionBookmark, Long> {

    Optional<PublicQuizCollectionBookmark> findByMemberAndPublicQuizCollection(Member member, PublicQuizCollection publicQuizCollection);
}
