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
    @Query("select c from Category c where c.member.id = :memberId")
    List<Category> findAllByMemberId(@Param("memberId") String memberId);

    Optional<Category> findByName(String name);

    Optional<Category> findByMember(Member member);

    Optional<Category> findByMemberAndId(Member member, Long categoryId);
}
