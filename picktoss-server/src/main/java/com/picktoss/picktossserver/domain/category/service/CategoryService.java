package com.picktoss.picktossserver.domain.category.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.category.controller.response.GetAllCategoriesResponse;
import com.picktoss.picktossserver.domain.category.controller.response.GetSingleCategoryResponse;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.category.repository.CategoryRepository;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.enums.CategoryTag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
            Set<Document> documents = category.getDocuments();

            List<GetAllCategoriesResponse.GetAllCategoriesDocumentDto> documentDtos = new ArrayList<>();
            for (Document document : documents) {
                GetAllCategoriesResponse.GetAllCategoriesDocumentDto documentDto = GetAllCategoriesResponse.GetAllCategoriesDocumentDto.builder()
                        .id(document.getId())
                        .name(document.getName())
                        .order(document.getOrder())
                        .documentStatus(document.getStatus())
                        .build();

                documentDtos.add(documentDto);
            }

            GetAllCategoriesResponse.GetAllCategoriesCategoryDto categoryDto = GetAllCategoriesResponse.GetAllCategoriesCategoryDto.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .tag(category.getTag())
                    .order(category.getOrder())
                    .emoji(category.getEmoji())
                    .documents(documentDtos)
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
                .order(category.getOrder())
                .build();

    }

    @Transactional
    public Long createCategory(String name, CategoryTag tag, Long memberId, Member member, String emoji) {
        Optional<Category> optionalCategory = categoryRepository.findByNameAndMemberId(name, memberId);
        if (optionalCategory.isPresent()) {
            throw new CustomException(DUPLICATE_CATEGORY);
        }

        Integer lastOrder = categoryRepository.findLastOrderByMemberId(memberId);
        if (lastOrder == null) {
            lastOrder = 0;
        }

        int order = lastOrder;

        Category category = Category.createCategory(member, name, tag, order + 1, emoji);
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

        categoryRepository.updateMinusOrderByDeletedOrder(memberId, category.getOrder());
        categoryRepository.delete(category);
    }

    @Transactional
    public void updateCategoryName(Long memberId, Long categoryId, String categoryName) {
        Category category = categoryRepository.findByCategoryIdAndMemberId(categoryId, memberId)
                .orElseThrow(() -> new CustomException(CATEGORY_NOT_FOUND));

        if (!Objects.equals(category.getMember().getId(), memberId)) {
            throw new CustomException(UNAUTHORIZED_OPERATION_EXCEPTION);
        }
        category.updateCategoryName(categoryName);
    }

    @Transactional
    public void updateCategoryTag(Long memberId, Long categoryId, CategoryTag tag) {
        Category category = categoryRepository.findByCategoryIdAndMemberId(categoryId, memberId)
                .orElseThrow(() -> new CustomException(CATEGORY_NOT_FOUND));

        if (!Objects.equals(category.getMember().getId(), memberId)) {
            throw new CustomException(UNAUTHORIZED_OPERATION_EXCEPTION);
        }
        category.updateCategoryTag(tag);
    }

    @Transactional
    public void updateCategoryEmoji(Long memberId, Long categoryId, String emoji) {
        Category category = categoryRepository.findByCategoryIdAndMemberId(categoryId, memberId)
                .orElseThrow(() -> new CustomException(CATEGORY_NOT_FOUND));

        if (!Objects.equals(category.getMember().getId(), memberId)) {
            throw new CustomException(UNAUTHORIZED_OPERATION_EXCEPTION);
        }
        category.updateCategoryEmoji(emoji);
    }

    @Transactional
    public void updateCategoryInfo(Long memberId, Long categoryId, String name, String emoji, CategoryTag categoryTag) {
        Category category = categoryRepository.findByCategoryIdAndMemberId(categoryId, memberId)
                .orElseThrow(() -> new CustomException(CATEGORY_NOT_FOUND));

        if (!Objects.equals(category.getMember().getId(), memberId)) {
            throw new CustomException(UNAUTHORIZED_OPERATION_EXCEPTION);
        }

        category.updateCategoryEmoji(emoji);
        category.updateCategoryName(name);
        category.updateCategoryTag(categoryTag);
    }

    @Transactional
    public void updateCategoriesOrder(Long categoryId, int preDragCategoryOrder, int afterDragCategoryOrder, Long memberId) {
        Category category = categoryRepository.findByCategoryIdAndMemberId(categoryId, memberId)
                .orElseThrow(() -> new CustomException(CATEGORY_NOT_FOUND));

        if (preDragCategoryOrder > afterDragCategoryOrder) {
            categoryRepository.updatePlusOrderByPreOrderGreaterThanAfterOrder(memberId, afterDragCategoryOrder, preDragCategoryOrder);
        } else {
            categoryRepository.updateMinusOrderByPreOrderLessThanAfterOrder(memberId, preDragCategoryOrder, afterDragCategoryOrder);
        }

        category.updateCategoryOrder(afterDragCategoryOrder);
    }

    public Category findByCategoryIdAndMemberId(Long categoryId, Long memberId) {
        return categoryRepository.findByCategoryIdAndMemberId(categoryId, memberId)
                .orElseThrow(() -> new CustomException(CATEGORY_NOT_FOUND));
    }
}
