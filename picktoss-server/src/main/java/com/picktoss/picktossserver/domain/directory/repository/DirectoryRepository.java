package com.picktoss.picktossserver.domain.directory.repository;


import com.picktoss.picktossserver.domain.directory.entity.Directory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DirectoryRepository extends JpaRepository<Directory, Long> {
    @Query("SELECT dir FROM Directory dir " +
            "LEFT JOIN FETCH dir.documents " +
            "WHERE dir.member.id = :memberId")
    List<Directory> findAllByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT dir FROM Directory dir " +
            "LEFT JOIN FETCH dir.documents d " +
            "LEFT JOIN FETCH d.quizzes " +
            "WHERE dir.id = :directoryId " +
            "AND dir.member.id = :memberId")
    Optional<Directory> findByDirectoryIdAndMemberId(
            @Param("directoryId") Long directoryId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT dir FROM Directory dir " +
            "JOIN FETCH dir.member m " +
            "JOIN FETCH m.star s " +
            "LEFT JOIN FETCH s.starHistories sh " +
            "WHERE dir.id = :directoryId " +
            "AND dir.member.id = :memberId")
    Optional<Directory> findDirectoryWithMemberAndStarAndStarHistoryByDirectoryIdAndMemberId(
            @Param("directoryId") Long directoryId,
            @Param("memberId") Long memberId
    );
}
