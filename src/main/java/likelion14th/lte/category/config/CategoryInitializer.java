package likelion14th.lte.category.config;

import likelion14th.lte.category.entity.Category;
import likelion14th.lte.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryInitializer implements ApplicationRunner {

    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "공부", "운동", "독서", "자기계발", "취미"
    );

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        DEFAULT_CATEGORIES.forEach(categoryName ->
                categoryRepository.findByCategoryName(categoryName)
                        .orElseGet(() -> categoryRepository.save(Category.create(categoryName)))
        );
    }
}
