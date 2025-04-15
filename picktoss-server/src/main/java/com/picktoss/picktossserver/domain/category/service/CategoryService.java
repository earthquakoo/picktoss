package com.picktoss.picktossserver.domain.category.service;

import com.picktoss.picktossserver.domain.category.dto.response.GetAllCategoriesResponse;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public GetAllCategoriesResponse findAllCategory() {
        List<Category> categories = categoryRepository.findAll();

        List<GetAllCategoriesResponse.GetAllCategoriesDto> categoriesDtos = new ArrayList<>();

        for (Category category : categories) {
            GetAllCategoriesResponse.GetAllCategoriesDto categoriesDto = GetAllCategoriesResponse.GetAllCategoriesDto.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .emoji(category.getEmoji())
                    .build();

            categoriesDtos.add(categoriesDto);
        }

        return new GetAllCategoriesResponse(categoriesDtos);
    }

    @Transactional
    public void createCategory(String name, String emoji) {
        Category category = Category.createCategory(name, emoji);
        categoryRepository.save(category);
    }
}
