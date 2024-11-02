package com.picktoss.picktossserver.domain.category.facade;

import com.picktoss.picktossserver.domain.category.controller.response.GetAllCategoriesResponse;
import com.picktoss.picktossserver.domain.category.controller.response.GetSingleCategoryResponse;
import com.picktoss.picktossserver.domain.category.service.CategoryService;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
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
    public Long createCategory(Long memberId, String name, String emoji) {
        Member member = memberService.findMemberById(memberId);
        return categoryService.createCategory(name, memberId, member, emoji);
    }

    @Transactional
    public void deleteCategory(Long memberId, Long categoryId) {
        categoryService.deleteCategory(memberId, categoryId);
    }

    @Transactional
    public void updateCategoryInfo(Long memberId, Long categoryId, String name, String emoji) {
        categoryService.updateCategoryInfo(memberId, categoryId, name, emoji);
    }
}
