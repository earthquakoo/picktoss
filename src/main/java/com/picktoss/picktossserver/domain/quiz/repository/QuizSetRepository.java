package com.picktoss.picktossserver.domain.quiz.repository;

import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizSetRepository extends JpaRepository<QuizSet, String> {

    @Query("SELECT qs FROM QuizSet qs WHERE qs.member.id = :memberId")
    List<QuizSet> findAllByMemberId(@Param("memberId") Long memberId);
}
