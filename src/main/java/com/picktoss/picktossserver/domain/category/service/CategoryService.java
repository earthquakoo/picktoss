package com.picktoss.picktossserver.domain.category.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.category.controller.response.CreateCategoryResponse;
import com.picktoss.picktossserver.domain.category.controller.response.GetAllCategoriesResponse;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.category.repository.CategoryRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;


    public List<GetAllCategoriesResponse.CategoryDto> findAllCategories(String memberId) {
        List<Category> categories = categoryRepository.findAllByMemberId(memberId);
        List<GetAllCategoriesResponse.CategoryDto> categoryDtos = new ArrayList<>();
        for (Category category : categories) {
            GetAllCategoriesResponse.CategoryDto categoryDto = GetAllCategoriesResponse.CategoryDto.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .build();

            categoryDtos.add(categoryDto);
        }
        return categoryDtos;

    }

    @Transactional
    public Long createCategory(Member member, String name) {
        Optional<Category> optionalCategory = categoryRepository.findByName(name);
        if (optionalCategory.isPresent()) {
            throw new CustomException(DUPLICATE_CATEGORY);
        }

        Category category = Category.builder()
                .name(name)
                .member(member)
                .build();

        categoryRepository.save(category);
        return category.getId();
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        Optional<Category> optionalCategory = categoryRepository.findById(categoryId);

        if (optionalCategory.isEmpty()) {
            return ;
        }

        Category category = optionalCategory.get();

        categoryRepository.delete(category);
    }

    @Transactional
    public void updateCategory(Long categoryId, String categoryName) {
        Optional<Category> optionalCategory = categoryRepository.findById(categoryId);

        if (optionalCategory.isEmpty()) {
            return ;
        }

        Category category = optionalCategory.get();
        category.updateCategoryName(categoryName);
    }

    public Category findCategoryByMemberAndCategoryId(Member member, Long categoryId) {
        return categoryRepository.findByMemberAndId(member, categoryId)
                .orElseThrow(() -> new CustomException(CATEGORY_NOT_FOUND));
    }


    public Category findCategoryByMember(Member member) {
        return categoryRepository.findByMember(member)
                .orElseThrow(() -> new CustomException((CATEGORY_NOT_FOUND)));
    }
}
