package com.picktoss.picktossserver.domain.keypoint.repository;


import com.picktoss.picktossserver.domain.keypoint.entity.KeyPointSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KeyPointSetRepository extends JpaRepository<KeyPointSet, String> {
    @Query("SELECT kps FROM KeyPointSet kps WHERE kps.member.id = :memberId")
    List<KeyPointSet> findAllByMemberId(@Param("memberId") Long memberId);
}
