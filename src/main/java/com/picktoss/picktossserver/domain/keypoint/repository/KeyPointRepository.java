package com.picktoss.picktossserver.domain.keypoint.repository;


import com.picktoss.picktossserver.domain.keypoint.entity.KeyPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KeyPointRepository extends JpaRepository<KeyPoint, Long> {

    @Query("SELECT k FROM KeyPoint k " +
            "JOIN FETCH k.document d " +
            "JOIN FETCH d.category c " +
            "WHERE c.member.id = :memberId AND k.bookmark = true")
    List<KeyPoint> findByBookmark(@Param("memberId") Long memberId);

    @Query("SELECT k FROM KeyPoint k " +
            "JOIN k.document d " +
            "JOIN d.category c " +
            "JOIN c.member m " +
            "WHERE d.id = :documentId AND m.id = :memberId"
    )
    List<KeyPoint> findAllByDocumentIdAndMemberId(
            @Param("documentId") Long documentId,
            @Param("memberId") Long memberId
    );
}
