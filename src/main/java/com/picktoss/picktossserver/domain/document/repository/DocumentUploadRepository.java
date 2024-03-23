package com.picktoss.picktossserver.domain.document.repository;

import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.entity.DocumentUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentUploadRepository extends JpaRepository<DocumentUpload, Long> {

    @Query("SELECT du FROM DocumentUpload du WHERE du.member.id = :memberId")
    List<Document> findAllByMemberId(@Param("memberId") String memberId);
}
