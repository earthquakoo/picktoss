package com.picktoss.picktossserver.domain.category.facade;

import com.picktoss.picktossserver.domain.category.controller.response.CreateCategoryResponse;
import com.picktoss.picktossserver.domain.category.controller.response.GetAllCategoriesResponse;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.category.repository.CategoryRepository;
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


    public List<GetAllCategoriesResponse.CategoryDto> findAllCategories(Long memberId) {
        return categoryService.findAllCategories(memberId);
    }

    @Transactional
    public Long createCategory(Long memberId, String name) {
        Member member = memberService.findMemberById(memberId);
        return categoryService.createCategory(name, memberId, member);
    }

    @Transactional
    public void deleteCategory(Long memberId, Long categoryId) {
        categoryService.deleteCategory(memberId, categoryId);
    }

    @Transactional
    public void updateCategory(Long memberId, Long categoryId, String categoryName) {
        categoryService.updateCategory(memberId, categoryId, categoryName);
    }

    public Category findByCategoryIdAndMemberId(Long categoryId, Long memberId) {
        return categoryService.findByCategoryIdAndMemberId(categoryId, memberId);
    }

    public Category findByMemberId(Long memberId) {
        return categoryService.findByMemberId(memberId);
    }
}
