package com.picktoss.picktossserver.domain.category.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.category.controller.response.GetAllCategoriesResponse;
import com.picktoss.picktossserver.domain.category.controller.response.GetSingleCategoryResponse;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.category.repository.CategoryRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<GetAllCategoriesResponse.GetAllCategoriesCategoryDto> findAllCategories(Long memberId) {
        List<Category> categories = categoryRepository.findAllByMemberId(memberId);
        List<GetAllCategoriesResponse.GetAllCategoriesCategoryDto> categoryDtos = new ArrayList<>();

        for (Category category : categories) {
            GetAllCategoriesResponse.GetAllCategoriesCategoryDto categoryDto = GetAllCategoriesResponse.GetAllCategoriesCategoryDto.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .tag(category.getTag())
                    .emoji(category.getEmoji())
                    .build();

            categoryDtos.add(categoryDto);
        }
        return categoryDtos;
    }

    public GetSingleCategoryResponse findSingleCategory(Long categoryId, Long memberId) {
        Category category = categoryRepository.findByCategoryIdAndMemberId(categoryId, memberId)
                .orElseThrow(() -> new CustomException(CATEGORY_NOT_FOUND));

        return GetSingleCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .tag(category.getTag())
                .emoji(category.getEmoji())
                .build();

    }

    @Transactional
    public Long createCategory(String name, Long memberId, Member member, String emoji) {
        Optional<Category> optionalCategory = categoryRepository.findByNameAndMemberId(name, memberId);
        if (optionalCategory.isPresent()) {
            throw new CustomException(DUPLICATE_CATEGORY);
        }

        Category category = Category.createCategory(member, name, emoji);
        categoryRepository.save(category);
        return category.getId();
    }

    @Transactional
    public Category createDefaultCategory(Member member) {
        Category category = Category.createDefaultCategory(member);
        categoryRepository.save(category);
        return category;
    }

    @Transactional
    public void deleteCategory(Long memberId, Long categoryId) {
        Category category = categoryRepository.findByCategoryIdAndMemberId(categoryId, memberId)
                .orElseThrow(() -> new CustomException(CATEGORY_NOT_FOUND));

        if (!Objects.equals(category.getMember().getId(), memberId)) {
            throw new CustomException(UNAUTHORIZED_OPERATION_EXCEPTION);
        }

        categoryRepository.delete(category);
    }

    @Transactional
    public void updateCategoryInfo(Long memberId, Long categoryId, String name, String emoji) {
        Category category = categoryRepository.findByCategoryIdAndMemberId(categoryId, memberId)
                .orElseThrow(() -> new CustomException(CATEGORY_NOT_FOUND));

        if (!Objects.equals(category.getMember().getId(), memberId)) {
            throw new CustomException(UNAUTHORIZED_OPERATION_EXCEPTION);
        }

        category.updateCategoryEmoji(emoji);
        category.updateCategoryName(name);
    }

    public Category findByCategoryIdAndMemberId(Long categoryId, Long memberId) {
        return categoryRepository.findByCategoryIdAndMemberId(categoryId, memberId)
                .orElseThrow(() -> new CustomException(CATEGORY_NOT_FOUND));
    }

    public Category findCategoryWithMemberAndStarAndStarHistoryByCategoryIdAndMemberId(Long categoryId, Long memberId) {
        return categoryRepository.findCategoryWithMemberAndStarAndStarHistoryByCategoryIdAndMemberId(categoryId, memberId)
                .orElseThrow(() -> new CustomException(CATEGORY_NOT_FOUND));
    }
}
