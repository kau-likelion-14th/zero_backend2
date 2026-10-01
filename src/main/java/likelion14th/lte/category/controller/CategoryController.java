package likelion14th.lte.category.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import likelion14th.lte.category.dto.response.CategoryResponse;
import likelion14th.lte.category.service.CategoryService;
import likelion14th.lte.global.api.ApiResponse;
import likelion14th.lte.global.api.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "카테고리 관리", description = "카테고리 관련 api")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    /** 카테고리 조회  **/
    @GetMapping
    @Operation(summary = "카테고리 목록 조회", description = "todo 추가 시에 보여줄 카테고리 목록을 조회합니다.")
    public ApiResponse<List<CategoryResponse>> getAllCategories (){
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ApiResponse.onSuccess(SuccessCode.CATEGORY_LIST_GET_SUCCESS, categories);
    }
}
