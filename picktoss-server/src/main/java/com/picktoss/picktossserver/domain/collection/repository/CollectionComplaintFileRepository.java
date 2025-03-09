package com.picktoss.picktossserver.domain.collection.repository;

import com.picktoss.picktossserver.domain.collection.entity.CollectionComplaintFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CollectionComplaintFileRepository extends JpaRepository<CollectionComplaintFile, Long> {

    @Query("SELECT ccf FROM CollectionComplaintFile ccf " +
            "WHERE ccf.collectionComplaint.id = :collectionComplaintId")
    List<CollectionComplaintFile> findAllByCollectionComplaintId(
            @Param("collectionComplaintId") Long collectionComplaintId
    );
}
