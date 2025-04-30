package com.picktoss.picktossserver.domain.document.repository;

import com.picktoss.picktossserver.domain.document.entity.DocumentComplaintFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentComplaintFileRepository extends JpaRepository<DocumentComplaintFile, Long> {

    @Query("SELECT dcf FROM DocumentComplaintFile dcf " +
            "WHERE dcf.documentComplaint.id = :documentComplaintId")
    List<DocumentComplaintFile> findAllByDocumentComplaintId(
            @Param("documentComplaintId") Long documentComplaintId
    );
}
