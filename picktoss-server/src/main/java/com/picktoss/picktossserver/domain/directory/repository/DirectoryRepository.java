package com.picktoss.picktossserver.domain.directory.repository;


import com.picktoss.picktossserver.domain.directory.entity.Directory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DirectoryRepository extends JpaRepository<Directory, Long> {
    @Query("SELECT d FROM Directory d " +
            "LEFT JOIN FETCH d.documents " +
            "WHERE d.member.id = :memberId")
    List<Directory> findAllByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT d FROM Directory d " +
            "WHERE d.id = :directoryId " +
            "AND d.member.id = :memberId")
    Optional<Directory> findByDirectoryIdAndMemberId(
            @Param("directoryId") Long directoryId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Directory d WHERE d.name = :name AND d.member.id = :memberId")
    Optional<Directory> findByNameAndMemberId(
            @Param("name") String name,
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Directory d " +
            "JOIN FETCH d.member m " +
            "JOIN FETCH m.star s " +
            "LEFT JOIN FETCH s.starHistories sh " +
            "WHERE d.id = :directoryId " +
            "AND d.member.id = :memberId")
    Optional<Directory> findDirectoryWithMemberAndStarAndStarHistoryByDirectoryIdAndMemberId(
            @Param("directoryId") Long directoryId,
            @Param("memberId") Long memberId
    );
}
