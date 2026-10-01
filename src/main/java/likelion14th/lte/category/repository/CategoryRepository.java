package likelion14th.lte.category.repository;

import likelion14th.lte.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByOrderByCategoryNameAsc();

    Optional<Category> findByCategoryName(String categoryName);
}
