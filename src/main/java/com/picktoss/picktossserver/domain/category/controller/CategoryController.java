package com.picktoss.picktossserver.domain.category.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.category.controller.request.CreateCategoryRequest;
import com.picktoss.picktossserver.domain.category.controller.request.UpdateCategoryNameRequest;
import com.picktoss.picktossserver.domain.category.controller.response.CreateCategoryResponse;
import com.picktoss.picktossserver.domain.category.controller.response.GetAllCategoriesResponse;
import com.picktoss.picktossserver.domain.category.facade.CategoryFacade;
import com.picktoss.picktossserver.domain.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryFacade categoryFacade;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/categories")
    public ResponseEntity<GetAllCategoriesResponse> getCategories() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        String memberId = jwtUserInfo.getMemberId();

        List<GetAllCategoriesResponse.CategoryDto> allCategories = categoryFacade.findAllCategories(memberId);
        return ResponseEntity.ok().body(new GetAllCategoriesResponse(allCategories));
    }

    @PostMapping("/categories")
    public ResponseEntity<CreateCategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        String memberId = jwtUserInfo.getMemberId();

        Long categoryId = categoryFacade.createCategory(memberId, request.getName());
        return ResponseEntity.ok().body(new CreateCategoryResponse(categoryId));
    }

    @DeleteMapping("/categories/{id}")
    public void deleteCategoryById(@PathVariable(name = "id") Long categoryId) {
        categoryFacade.deleteCategory(categoryId);
    }

    @PatchMapping("/categories/{id}")
    public void updateCategoryById(@PathVariable(name = "id") Long categoryId, @Valid @RequestBody UpdateCategoryNameRequest request) {
        categoryFacade.updateCategory(categoryId, request.getCategoryName());
    }
}
