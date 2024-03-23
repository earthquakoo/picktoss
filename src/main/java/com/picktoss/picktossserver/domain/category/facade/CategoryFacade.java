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


    public List<GetAllCategoriesResponse.CategoryDto> findAllCategories(String memberId) {
        return categoryService.findAllCategories(memberId);
    }

    @Transactional
    public Long createCategory(String memberId, String name) {
        Member member = memberService.findMemberById(memberId);
        return categoryService.createCategory(member, name);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        categoryService.deleteCategory(categoryId);
    }

    @Transactional
    public void updateCategory(Long categoryId, String categoryName) {
        categoryService.updateCategory(categoryId, categoryName);
    }

    public Category findCategoryByMemberAndCategoryId(Member member, Long categoryId) {
        return categoryService.findCategoryByMemberAndCategoryId(member, categoryId);
    }

    public Category findCategoryByMember(Member member) {
        return categoryService.findCategoryByMember(member);
    }
}
