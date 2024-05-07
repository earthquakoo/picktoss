package com.picktoss.picktossserver.domain.category.repository;


import com.picktoss.picktossserver.domain.category.controller.dto.CategoryResponseDto;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT c FROM Category c WHERE c.member.id = :memberId")
    List<Category> findAllByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT c FROM Category c WHERE c.id = :categoryId AND c.member.id = :memberId")
    Optional<Category> findByCategoryIdAndMemberId(
            @Param("categoryId") Long categoryId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT c FROM Category c WHERE c.name = :name AND c.member.id = :memberId")
    Optional<Category> findByNameAndMemberId(
            @Param("name") String name,
            @Param("memberId") Long memberId
    );

    @Query("SELECT MAX(c.order) FROM Category c WHERE c.member.id = :memberId")
    Integer findLastOrderByMemberId(@Param("memberId") Long memberId);
}
