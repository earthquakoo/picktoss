package com.picktoss.picktossserver.domain.keypoint.repository;


import com.picktoss.picktossserver.domain.keypoint.entity.KeyPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface KeyPointRepository extends JpaRepository<KeyPoint, Long> {

    @Query("SELECT k FROM KeyPoint k WHERE k.bookmark = true")
    List<KeyPoint> findByBookmark();
}
