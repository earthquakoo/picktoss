package com.picktoss.picktossserver.domain.question.repository;

import com.picktoss.picktossserver.domain.question.entity.QuestionSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionSetRepository extends JpaRepository<QuestionSet, String> {
    @Query("SELECT qs FROM QuestionSet qs WHERE qs.member.id = :memberId")
    List<QuestionSet> findAllByMemberId(@Param("memberId") Long memberId);
}
