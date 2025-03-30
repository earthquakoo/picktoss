package com.picktoss.picktossserver.domain.publicquizcollection.repository;

import com.picktoss.picktossserver.domain.publicquizcollection.entity.PublicQuizCollection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicQuizCollectionRepository extends JpaRepository<PublicQuizCollection, Long> {
}
