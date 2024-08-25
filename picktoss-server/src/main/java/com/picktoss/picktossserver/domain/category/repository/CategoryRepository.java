package com.picktoss.picktossserver.domain.category.repository;


import com.picktoss.picktossserver.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT c FROM Category c " +
            "LEFT JOIN FETCH c.documents " +
            "WHERE c.member.id = :memberId " +
            "ORDER BY c.order ASC")
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

    @Transactional
    @Modifying
    @Query("UPDATE Category c SET c.order = c.order + 1 WHERE c.member.id = :memberId AND c.order >= :minCategoryOrder AND c.order < :maxCategoryOrder")
    void updatePlusOrderByPreOrderGreaterThanAfterOrder(
            @Param("memberId") Long memberId,
            @Param("minCategoryOrder") int minCategoryOrder,
            @Param("maxCategoryOrder") int maxCategoryOrder
    );

    @Transactional
    @Modifying
    @Query("UPDATE Category c SET c.order = c.order - 1 WHERE c.member.id = :memberId AND c.order > :minCategoryOrder AND c.order <= :maxCategoryOrder")
    void updateMinusOrderByPreOrderLessThanAfterOrder(
            @Param("memberId") Long memberId,
            @Param("minCategoryOrder") int minCategoryOrder,
            @Param("maxCategoryOrder") int maxCategoryOrder
    );

    @Transactional
    @Modifying
    @Query("UPDATE Category c SET c.order = c.order - 1 WHERE c.member.id = :memberId AND c.order > :deletedOrder")
    void updateMinusOrderByDeletedOrder(
            @Param("memberId") Long memberId,
            @Param("deletedOrder") int deletedOrder
    );

//    @Query("SELECT c FROM Category c " +
//            "WHERE c.order >= :minDocumentOrder AND c.order < :maxDocumentOrder " +
//            "AND c.member.id = :memberId " +
//            "ORDER BY c.order ASC")
//    List<Category> findByOrderGreaterThanEqualAndOrderLessThanOrderByOrderAsc(
//            @Param("minCategoryOrder") int minCategoryOrder,
//            @Param("maxCategoryOrder") int maxCategoryOrder,
//            @Param("memberId") Long memberId
//    );
//
//    @Query("SELECT c FROM Category c " +
//            "WHERE c.order > :minDocumentOrder AND c.order <= :maxDocumentOrder " +
//            "AND c.member.id = :memberId " +
//            "ORDER BY c.order ASC")
//    List<Category> findByOrderGreaterThanAndOrderLessThanEqualOrderByOrderAsc(
//            @Param("minCategoryOrder") int minCategoryOrder,
//            @Param("maxCategoryOrder") int maxCategoryOrder,
//            @Param("memberId") Long memberId
//    );
}
