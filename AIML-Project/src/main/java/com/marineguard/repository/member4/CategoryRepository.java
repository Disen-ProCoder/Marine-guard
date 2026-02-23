package com.marineguard.repository.member4;

import com.marineguard.model.member4.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {

    // Find by active status with sorting
    List<Category> findByIsActiveTrueOrderByDisplayOrderAscNameAsc();

    // Find by parent category
    List<Category> findByParentCategoryOrderByDisplayOrderAscNameAsc(Category parent);

    // Find categories with no parent
    List<Category> findByParentCategoryIsNullOrderByDisplayOrderAscNameAsc();

    // Find by name (case-insensitive exact match)
    Optional<Category> findByNameIgnoreCase(String name);

    // Find by name containing (case-insensitive)
    List<Category> findByNameContainingIgnoreCase(String name);

    // Find by tag
    @Query("{ 'tags': { $in: [?0] } }")
    List<Category> findByTagsContaining(String tag);

    // Check if exists by name (case-insensitive)
    boolean existsByNameIgnoreCase(String name);

    // Find all with sorting
    List<Category> findAllByOrderByDisplayOrderAscNameAsc();

    // Find max display order
    @Query(value = "{}", sort = "{ 'displayOrder': -1 }")
    List<Category> findTopByOrderByDisplayOrderDesc();

    // Custom query to get max display order
    @Query(value = "{}", fields = "{ 'displayOrder': 1 }")
    List<Integer> findAllDisplayOrders();

    // Helper method to get max display order
    default Integer findMaxDisplayOrder() {
        List<Category> top = findTopByOrderByDisplayOrderDesc();
        return top.isEmpty() ? null : top.get(0).getDisplayOrder();
    }

    // Find by parent ID
    @Query("{ 'parentCategory.$id' : ?0 }")
    List<Category> findByParentCategoryId(String parentId);

    // Count categories by active status
    long countByIsActiveTrue();
    long countByIsActiveFalse();
}