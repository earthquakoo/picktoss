package com.picktoss.picktossserver.domain.feedback.repository;

import com.picktoss.picktossserver.domain.feedback.entity.FeedbackFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackFileRepository extends JpaRepository<FeedbackFile, Long> {
}
