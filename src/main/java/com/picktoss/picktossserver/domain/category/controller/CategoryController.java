package com.picktoss.picktossserver.domain.category.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.category.controller.request.*;
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

import java.util.ArrayList;
import java.util.List;

@Tag(name = "2. Category")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class CategoryController {

    private final CategoryFacade categoryFacade;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "Get all categories")
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

    @Operation(summary = "Create category")
    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CreateCategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        Long categoryId = categoryFacade.createCategory(memberId, request.getName(), request.getTag(), request.getEmoji());
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateCategoryResponse(categoryId));
    }

    @Operation(summary = "Delete category")
    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable(name = "id") Long categoryId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();
        categoryFacade.deleteCategory(memberId, categoryId);
    }

    @Operation(summary = "Update category name")
    @PatchMapping("/categories/name/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCategoryName(@PathVariable(name = "id") Long categoryId, @Valid @RequestBody UpdateCategoryNameRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();
        categoryFacade.updateCategoryName(memberId, categoryId, request.getCategoryName());
    }

    @Operation(summary = "Update category tag")
    @PatchMapping("/categories/tag/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCategoryTag(@PathVariable(name = "id") Long categoryId, @Valid @RequestBody UpdateCategoryTagRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        categoryFacade.updateCategoryTag(memberId, categoryId, request.getTag());
    }

    @Operation(summary = "Update category emoji")
    @PatchMapping("/categories/emoji/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCategoryEmoji(
            @PathVariable(name = "id") Long categoryId,
            @Valid @RequestBody UpdateCategoryEmojiRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        categoryFacade.updateCategoryEmoji(memberId, categoryId, request.getEmoji());
    }

    @Operation(summary = "update category info")
    @PatchMapping("/categories/info/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCategoryInfo(
            @PathVariable(name = "id") Long categoryId,
            @Valid @RequestBody UpdateCategoryInfoRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        categoryFacade.updateCategoryInfo(memberId, categoryId, request.getName(), request.getEmoji(), request.getCategoryTag());
    }

    @Operation(summary = "Update categories order")
    @PatchMapping("/categories/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCategoriesOrder(@Valid @RequestBody UpdateCategoriesOrderRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        categoryFacade.updateCategoriesOrder(request.getCategoryId(), request.getPreDragCategoryOrder(), request.getAfterDragCategoryOrder(), memberId);
    }
}
