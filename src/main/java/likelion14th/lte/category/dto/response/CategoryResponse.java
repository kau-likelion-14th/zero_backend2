package likelion14th.lte.category.dto.response;

import likelion14th.lte.category.entity.Category;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryResponse {
    private Long categoryId;
    private String categoryName;

    public static CategoryResponse from(Category category){
        return new CategoryResponse(
                category.getId(),
                category.getCategoryName()
        );
    }
}
