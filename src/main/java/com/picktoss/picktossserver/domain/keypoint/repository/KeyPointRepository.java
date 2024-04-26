package com.picktoss.picktossserver.domain.keypoint.repository;


import com.picktoss.picktossserver.domain.keypoint.entity.KeyPoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeyPointRepository extends JpaRepository<KeyPoint, Long> {
}
