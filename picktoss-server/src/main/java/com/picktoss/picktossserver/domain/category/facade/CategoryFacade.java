package com.picktoss.picktossserver.domain.category.facade;

import com.picktoss.picktossserver.domain.category.controller.response.GetAllCategoriesResponse;
import com.picktoss.picktossserver.domain.category.controller.response.GetSingleCategoryResponse;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.category.service.CategoryService;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import com.picktoss.picktossserver.global.enums.CategoryTag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryFacade {

    private final CategoryService categoryService;
    private final MemberService memberService;


    public List<GetAllCategoriesResponse.GetAllCategoriesCategoryDto> findAllCategories(Long memberId) {
        return categoryService.findAllCategories(memberId);
    }

    public GetSingleCategoryResponse findSingleCategory(Long categoryId, Long memberId) {
        return categoryService.findSingleCategory(categoryId, memberId);
    }

    @Transactional
    public Long createCategory(Long memberId, String name, CategoryTag tag, String emoji) {
        Member member = memberService.findMemberById(memberId);
        return categoryService.createCategory(name, tag, memberId, member, emoji);
    }

    @Transactional
    public void deleteCategory(Long memberId, Long categoryId) {
        categoryService.deleteCategory(memberId, categoryId);
    }

    @Transactional
    public void updateCategoryName(Long memberId, Long categoryId, String categoryName) {
        categoryService.updateCategoryName(memberId, categoryId, categoryName);
    }

    @Transactional
    public void updateCategoryTag(Long memberId, Long categoryId, CategoryTag tag) {
        categoryService.updateCategoryTag(memberId, categoryId, tag);
    }

    @Transactional
    public void updateCategoryEmoji(Long memberId, Long categoryId, String emoji) {
        categoryService.updateCategoryEmoji(memberId, categoryId, emoji);
    }

    @Transactional
    public void updateCategoryInfo(Long memberId, Long categoryId, String name, String emoji, CategoryTag categoryTag) {
        categoryService.updateCategoryInfo(memberId, categoryId, name, emoji, categoryTag);
    }

    @Transactional
    public void updateCategoriesOrder(Long categoryId, int preDragCategoryOrder, int afterDragCategoryOrder, Long memberId) {
        categoryService.updateCategoriesOrder(categoryId, preDragCategoryOrder, afterDragCategoryOrder, memberId);
    }

    public Category findByCategoryIdAndMemberId(Long categoryId, Long memberId) {
        return categoryService.findByCategoryIdAndMemberId(categoryId, memberId);
    }
}
