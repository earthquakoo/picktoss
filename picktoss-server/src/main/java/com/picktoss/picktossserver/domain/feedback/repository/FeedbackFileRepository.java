package com.picktoss.picktossserver.domain.feedback.repository;

import com.picktoss.picktossserver.domain.feedback.entity.FeedbackFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedbackFileRepository extends JpaRepository<FeedbackFile, Long> {

    @Query("SELECT ff FROM FeedbackFile ff " +
            "WHERE ff.feedback.id = :feedbackId")
    List<FeedbackFile> findAllByFeedbackId(
            @Param("feedbackId") Long feedbackId
    );

}
