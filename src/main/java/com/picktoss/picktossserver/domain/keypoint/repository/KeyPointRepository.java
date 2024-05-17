package com.picktoss.picktossserver.domain.keypoint.repository;


import com.picktoss.picktossserver.domain.keypoint.entity.KeyPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KeyPointRepository extends JpaRepository<KeyPoint, Long> {

    @Query("SELECT k FROM KeyPoint k " +
            "JOIN k.document d " +
            "JOIN d.category c " +
            "JOIN c.member m " +
            "WHERE m.id = :memberId AND k.bookmark = true")
    List<KeyPoint> findByBookmark(@Param("memberId") Long memberId);

//    @Query("SELECT k FROM KeyPoint k " +
//            "JOIN k.document d " +
//            "JOIN d.category c " +
//            "JOIN c.member m " +
//            "WHERE m.id = :memberId AND k.bookmark = true")
//    List<KeyPoint> findBookmarkedByMemberId(@Param("memberId") Long memberId);
}
