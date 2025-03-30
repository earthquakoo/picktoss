package com.picktoss.picktossserver.domain.publicquizcollection.repository;

import com.picktoss.picktossserver.domain.collection.entity.CollectionComplaintFile;
import com.picktoss.picktossserver.domain.publicquizcollection.entity.PublicQuizCollectionComplaintFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PublicQuizCollectionComplaintFileRepository extends JpaRepository<PublicQuizCollectionComplaintFile, Long> {
    @Query("SELECT ccf FROM PublicQuizCollectionComplaintFile ccf " +
            "WHERE ccf.publicQuizCollectionComplaint.id = :publicQuizCollectionComplaintId")
    List<CollectionComplaintFile> findAllByPublicQuizCollectionComplaintId(
            @Param("publicQuizCollectionComplaintId") Long publicQuizCollectionComplaintId
    );
}
