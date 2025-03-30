package com.picktoss.picktossserver.domain.publicquizcollection.repository;

import com.picktoss.picktossserver.domain.publicquizcollection.entity.PublicQuizCollection;
import com.picktoss.picktossserver.global.enums.document.PublicQuizCollectionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PublicQuizCollectionRepository extends JpaRepository<PublicQuizCollection, Long> {

    @Query("SELECT pqc FROM PublicQuizCollection pqc " +
            "JOIN FETCH pqc.document d " +
            "JOIN FETCH d.quizzes " +
            "LEFT JOIN FETCH pqc.publicQuizCollectionBookmarks " +
            "WHERE pqc.publicQuizCollectionCategory IN :publicQuizCollectionCategories")
    List<PublicQuizCollection> findAllByPublicQuizCollectionCategories(
            @Param("publicQuizCollectionCategories") List<PublicQuizCollectionCategory> publicQuizCollectionCategories
    );

    @Query("SELECT pqc FROM PublicQuizCollection pqc " +
            "LEFT JOIN FETCH pqc.publicQuizCollectionBookmarks pqcb " +
            "WHERE pqcb.member.id = :memberId")
    List<PublicQuizCollection> findAllByMemberIdAndBookmarked(
            @Param("memberId") Long memberId
    );
}