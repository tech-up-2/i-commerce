package com.example.i_commerce.domain.product.repository;

import com.example.i_commerce.domain.product.repository.projection.CategoryTreeRow;
import com.example.i_commerce.domain.product.entity.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByParentIsNullAndName(String name);

    boolean existsByParentAndName(Category parent, String name);

    @Query(value = """
        WITH RECURSIVE category_tree AS (
            SELECT id, parent_id, name, depth, 0 AS relative_depth
            FROM categories
            WHERE parent_id IS NULL

            UNION ALL

            SELECT c.id, c.parent_id, c.name, c.depth, ct.relative_depth + 1
            FROM categories c
            INNER JOIN category_tree ct ON c.parent_id = ct.id
            WHERE ct.relative_depth < :maxDepth
        )
        SELECT id, parent_id, name, depth
        FROM category_tree
        ORDER BY depth, id
        """, nativeQuery = true)
    List<CategoryTreeRow> findAllCategoryTree(@Param("maxDepth") int maxDepth);

    @Query(value = """
        WITH RECURSIVE category_tree AS (
            SELECT id, parent_id, name, depth, 0 AS relative_depth
            FROM categories
            WHERE id = :rootId

            UNION ALL

            SELECT c.id, c.parent_id, c.name, c.depth, ct.relative_depth + 1
            FROM categories c
            INNER JOIN category_tree ct ON c.parent_id = ct.id
            WHERE ct.relative_depth < :maxDepth
        )
        SELECT id, parent_id, name, depth
        FROM category_tree
        ORDER BY depth, id
        """, nativeQuery = true)
    List<CategoryTreeRow> findCategoryTreeById(
        @Param("rootId") Long rootId,
        @Param("maxDepth") int maxDepth
    );

    @Query(value = """
        WITH RECURSIVE category_tree AS (
            SELECT id, 0 AS depth
            FROM categories
            WHERE id = :categoryId

            UNION ALL

            SELECT c.id, ct.depth + 1
            FROM categories c
            INNER JOIN category_tree ct ON c.parent_id = ct.id
            WHERE ct.depth < 10
        )
        SELECT id
        FROM category_tree
        """, nativeQuery = true)
    List<Long> findAllDescendantIds(@Param("categoryId") Long categoryId);
}
