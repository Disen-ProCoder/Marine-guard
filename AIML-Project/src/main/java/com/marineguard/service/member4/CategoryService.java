package com.marineguard.service.member4;

import com.marineguard.model.member4.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryService {

    // ==================== BASIC CRUD ====================

    /**
     * Save a new category or update existing one
     */
    Category save(Category category);

    /**
     * Find category by ID
     */
    Optional<Category> findById(String id);

    /**
     * Find all categories
     */
    List<Category> findAll();

    /**
     * Find all categories by IDs
     */
    List<Category> findAllById(Iterable<String> ids);

    /**
     * Update an existing category
     */
    Category update(String id, Category categoryDetails);

    /**
     * Delete category by ID
     */
    void deleteById(String id);

    /**
     * Delete multiple categories
     */
    void deleteAll(List<Category> categories);

    // ==================== CUSTOM QUERIES ====================

    /**
     * Find only active categories
     */
    List<Category> findByIsActiveTrue();

    /**
     * Find categories by parent category
     */
    List<Category> findByParentCategory(Category parent);

    /**
     * Find categories with no parent (top-level categories)
     */
    List<Category> findByParentCategoryIsNull();

    /**
     * Find categories by name (partial match, case-insensitive)
     */
    List<Category> findByNameContaining(String name);

    /**
     * Find categories by exact name (case-insensitive)
     */
    Optional<Category> findByNameIgnoreCase(String name);

    /**
     * Find categories by tag
     */
    List<Category> findByTagsContaining(String tag);

    // ==================== BUSINESS LOGIC ====================

    /**
     * Check if category exists by name
     */
    boolean existsByName(String name);

    /**
     * Check if category has any library items
     */
    boolean hasLibraryItems(String categoryId);

    /**
     * Get next available display order number
     */
    int getNextDisplayOrder();

    /**
     * Validate if a category can be a parent
     */
    boolean isValidParentCategory(Category category);

    /**
     * Get category hierarchy path (parent chain)
     */
    List<Category> getCategoryHierarchy(String categoryId);

    /**
     * Get all descendant categories (children, grandchildren, etc.)
     */
    List<Category> getAllDescendants(String categoryId);

    /**
     * Count total items in category (including subcategories)
     */
    long countTotalItemsInCategory(String categoryId);

    // ==================== BULK OPERATIONS ====================

    /**
     * Save multiple categories at once
     */
    List<Category> saveAll(Iterable<Category> categories);

    /**
     * Bulk delete categories by IDs
     */
    void bulkDelete(List<String> ids);

    /**
     * Bulk create categories
     */
    List<Category> bulkCreate(List<Category> categories);

    /**
     * Bulk update category status
     */
    void bulkUpdateStatus(List<String> ids, boolean isActive);

    // ==================== STATISTICS ====================

    /**
     * Get category statistics
     */
    java.util.Map<String, Object> getCategoryStatistics();

    /**
     * Get popular categories (most items)
     */
    List<Category> getPopularCategories(int limit);

    /**
     * Get category tree structure
     */
    List<java.util.Map<String, Object>> getCategoryTree();
}