package com.picktoss.picktossserver.domain.feedback.repository;

import com.picktoss.picktossserver.domain.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

}
