package com.picktoss.picktossserver.domain.category.controller;

import com.picktoss.picktossserver.domain.category.dto.request.CreateCategoryRequest;
import com.picktoss.picktossserver.domain.category.dto.response.GetAllCategoriesResponse;
import com.picktoss.picktossserver.domain.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Category")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "카테고리 정보 가져오기")
    @GetMapping("/categories")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetAllCategoriesResponse> getAllCategories() {

        GetAllCategoriesResponse response = categoryService.findAllCategory();
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "카테고리 생성(운영 API)")
    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public void createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        categoryService.createCategory(request.getName(), request.getEmoji(), request.getColor(), request.getOrders());
    }
}
