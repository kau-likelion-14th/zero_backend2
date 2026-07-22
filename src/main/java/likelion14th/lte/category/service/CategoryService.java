package likelion14th.lte.category.service;

import likelion14th.lte.category.entity.Category;
import likelion14th.lte.category.dto.response.CategoryResponse;
import likelion14th.lte.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    /** 카테고리 목록 조회 **/
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories(){
        // 카테고리 목록 조회
        List<Category> categories = categoryRepository.findAllByOrderByCategoryNameAsc();

        // Dto 로 변환 후 반환
        return categories.stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }
}
