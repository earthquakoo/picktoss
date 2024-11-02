package com.picktoss.picktossserver.domain.category.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.category.controller.request.CreateCategoryRequest;
import com.picktoss.picktossserver.domain.category.controller.request.UpdateCategoryInfoRequest;
import com.picktoss.picktossserver.domain.category.controller.response.CreateCategoryResponse;
import com.picktoss.picktossserver.domain.category.controller.response.GetAllCategoriesResponse;
import com.picktoss.picktossserver.domain.category.controller.response.GetSingleCategoryResponse;
import com.picktoss.picktossserver.domain.category.facade.CategoryFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Category")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class CategoryController {

    private final CategoryFacade categoryFacade;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "모든 카테고리 가져오기")
    @GetMapping("/categories")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetAllCategoriesResponse> getCategories() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<GetAllCategoriesResponse.GetAllCategoriesCategoryDto> allCategories = categoryFacade.findAllCategories(memberId);
        return ResponseEntity.ok().body(new GetAllCategoriesResponse(allCategories));
    }

    @Operation(summary = "Get category by id")
    @GetMapping("/categories/{category_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetSingleCategoryResponse> getSingleCategory(@PathVariable("category_id") Long categoryId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetSingleCategoryResponse response = categoryFacade.findSingleCategory(categoryId, memberId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "카테고리 생성")
    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CreateCategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        Long categoryId = categoryFacade.createCategory(memberId, request.getName(), request.getEmoji());
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateCategoryResponse(categoryId));
    }

    @Operation(summary = "카테고리 삭제")
    @DeleteMapping("/categories/{category_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable(name = "category_id") Long categoryId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();
        categoryFacade.deleteCategory(memberId, categoryId);
    }

    @Operation(summary = "카테고리 정보 변경")
    @PatchMapping("/categories/{category_id}/update-info")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCategoryInfo(
            @PathVariable(name = "category_id") Long categoryId,
            @Valid @RequestBody UpdateCategoryInfoRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        categoryFacade.updateCategoryInfo(memberId, categoryId, request.getName(), request.getEmoji());
    }
}
