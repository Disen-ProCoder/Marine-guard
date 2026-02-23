package com.marineguard.service.member4;

import com.marineguard.model.member4.Category;
import com.marineguard.model.member4.LibraryItem;
import com.marineguard.repository.member4.CategoryRepository;
import com.marineguard.repository.member4.LibraryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final LibraryRepository libraryRepository;

    // ==================== BASIC CRUD ====================

    @Override
    public Category save(Category category) {
        log.info("Saving category: {}", category.getName());

        // Business validations
        validateCategory(category);

        // Set timestamps if new
        if (category.getId() == null) {
            category.setCreatedAt(LocalDateTime.now());
            category.setUpdatedAt(LocalDateTime.now());
        } else {
            category.setUpdatedAt(LocalDateTime.now());
        }

        // Auto-set display order if not provided
        if (category.getDisplayOrder() == null) {
            category.setDisplayOrder(getNextDisplayOrder());
        }

        return categoryRepository.save(category);
    }

    @Override
    public Optional<Category> findById(String id) {
        return categoryRepository.findById(id);
    }

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAllByOrderByDisplayOrderAscNameAsc();
    }

    @Override
    public List<Category> findAllById(Iterable<String> ids) {
        return categoryRepository.findAllById(ids);
    }

    @Override
    public Category update(String id, Category categoryDetails) {
        log.info("Updating category with id: {}", id);

        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Update fields
        existingCategory.setName(categoryDetails.getName());
        existingCategory.setDescription(categoryDetails.getDescription());
        existingCategory.setIconUrl(categoryDetails.getIconUrl());
        existingCategory.setParentCategory(categoryDetails.getParentCategory());
        existingCategory.setTags(categoryDetails.getTags());
        existingCategory.setDisplayOrder(categoryDetails.getDisplayOrder());
        existingCategory.setIsActive(categoryDetails.getIsActive());
        existingCategory.setUpdatedAt(LocalDateTime.now());

        // Validate after update
        validateCategory(existingCategory);

        return categoryRepository.save(existingCategory);
    }

    @Override
    public void deleteById(String id) {
        log.info("Deleting category with id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Check if category has subcategories
        List<Category> subCategories = categoryRepository.findByParentCategory(category);
        if (!subCategories.isEmpty()) {
            throw new RuntimeException("Cannot delete category with subcategories. Delete subcategories first.");
        }

        // Check if category has library items
        if (hasLibraryItems(id)) {
            throw new RuntimeException("Cannot delete category with library items. Move items to another category first.");
        }

        categoryRepository.delete(category);
    }

    @Override
    public void deleteAll(List<Category> categories) {
        log.info("Deleting {} categories", categories.size());

        for (Category category : categories) {
            // Check each category before deletion
            if (hasLibraryItems(category.getId())) {
                throw new RuntimeException("Cannot delete category '" + category.getName() + "' because it has library items");
            }
        }

        categoryRepository.deleteAll(categories);
    }

    // ==================== CUSTOM QUERIES ====================

    @Override
    public List<Category> findByIsActiveTrue() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAscNameAsc();
    }

    @Override
    public List<Category> findByParentCategory(Category parent) {
        return categoryRepository.findByParentCategoryOrderByDisplayOrderAscNameAsc(parent);
    }

    @Override
    public List<Category> findByParentCategoryIsNull() {
        return categoryRepository.findByParentCategoryIsNullOrderByDisplayOrderAscNameAsc();
    }

    @Override
    public List<Category> findByNameContaining(String name) {
        return categoryRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public Optional<Category> findByNameIgnoreCase(String name) {
        return categoryRepository.findByNameIgnoreCase(name);
    }

    @Override
    public List<Category> findByTagsContaining(String tag) {
        return categoryRepository.findByTagsContaining(tag);
    }

    // ==================== BUSINESS LOGIC ====================

    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public boolean hasLibraryItems(String categoryId) {
        Optional<Category> category = findById(categoryId);
        return category.isPresent() && !libraryRepository.findByCategory(category.get()).isEmpty();
    }

    @Override
    public int getNextDisplayOrder() {
        Integer maxOrder = categoryRepository.findMaxDisplayOrder();
        return (maxOrder == null) ? 1 : maxOrder + 1;
    }

    @Override
    public boolean isValidParentCategory(Category category) {
        // A category cannot be its own parent
        if (category.getParentCategory() != null &&
                category.getId() != null &&
                category.getId().equals(category.getParentCategory().getId())) {
            return false;
        }

        // Check for circular reference
        return !wouldCreateCircularReference(category);
    }

    @Override
    public List<Category> getCategoryHierarchy(String categoryId) {
        List<Category> hierarchy = new ArrayList<>();
        Optional<Category> current = findById(categoryId);

        while (current.isPresent()) {
            hierarchy.add(0, current.get()); // Add to beginning
            current = Optional.ofNullable(current.get().getParentCategory());
        }

        return hierarchy;
    }

    @Override
    public List<Category> getAllDescendants(String categoryId) {
        List<Category> descendants = new ArrayList<>();
        Optional<Category> category = findById(categoryId);

        if (category.isPresent()) {
            collectDescendants(category.get(), descendants);
        }

        return descendants;
    }

    @Override
    public long countTotalItemsInCategory(String categoryId) {
        List<Category> allCategories = new ArrayList<>();
        Optional<Category> category = findById(categoryId);

        if (category.isPresent()) {
            allCategories.add(category.get());
            allCategories.addAll(getAllDescendants(categoryId));
        }

        long total = 0;
        for (Category cat : allCategories) {
            total += libraryRepository.findByCategory(cat).size();
        }

        return total;
    }

    // ==================== BULK OPERATIONS ====================

    @Override
    public List<Category> saveAll(Iterable<Category> categories) {
        List<Category> categoryList = new ArrayList<>();
        categories.forEach(categoryList::add);

        log.info("Saving {} categories", categoryList.size());

        for (Category category : categoryList) {
            if (category.getId() == null) {
                category.setCreatedAt(LocalDateTime.now());
            }
            category.setUpdatedAt(LocalDateTime.now());
            validateCategory(category);
        }

        return categoryRepository.saveAll(categoryList);
    }

    @Override
    public void bulkDelete(List<String> ids) {
        log.info("Bulk deleting {} categories", ids.size());

        for (String id : ids) {
            deleteById(id); // Uses single delete with validations
        }
    }

    @Override
    public List<Category> bulkCreate(List<Category> categories) {
        log.info("Bulk creating {} categories", categories.size());

        for (Category category : categories) {
            category.setCreatedAt(LocalDateTime.now());
            category.setUpdatedAt(LocalDateTime.now());
            category.setIsActive(true);

            if (category.getDisplayOrder() == null) {
                category.setDisplayOrder(getNextDisplayOrder());
            }

            validateCategory(category);
        }

        return categoryRepository.saveAll(categories);
    }

    @Override
    public void bulkUpdateStatus(List<String> ids, boolean isActive) {
        log.info("Bulk updating status for {} categories to: {}", ids.size(), isActive);

        List<Category> categories = categoryRepository.findAllById(ids);
        for (Category category : categories) {
            category.setIsActive(isActive);
            category.setUpdatedAt(LocalDateTime.now());
        }

        categoryRepository.saveAll(categories);
    }

    // ==================== STATISTICS ====================

    @Override
    public Map<String, Object> getCategoryStatistics() {
        Map<String, Object> stats = new HashMap<>();

        List<Category> allCategories = findAll();
        long totalCategories = allCategories.size();
        long activeCategories = allCategories.stream().filter(Category::getIsActive).count();
        long parentCategories = allCategories.stream()
                .filter(c -> c.getParentCategory() == null)
                .count();

        // Count items per category
        Map<String, Long> itemsPerCategory = new HashMap<>();
        for (Category category : allCategories) {
            long count = libraryRepository.findByCategory(category).size();
            itemsPerCategory.put(category.getName(), count);
        }

        stats.put("totalCategories", totalCategories);
        stats.put("activeCategories", activeCategories);
        stats.put("inactiveCategories", totalCategories - activeCategories);
        stats.put("parentCategories", parentCategories);
        stats.put("subCategories", totalCategories - parentCategories);
        stats.put("itemsPerCategory", itemsPerCategory);
        stats.put("mostPopularCategory", getMostPopularCategory());

        return stats;
    }

    @Override
    public List<Category> getPopularCategories(int limit) {
        return categoryRepository.findAll().stream()
                .filter(Category::getIsActive)
                .sorted((c1, c2) -> {
                    long count1 = libraryRepository.findByCategory(c1).size();
                    long count2 = libraryRepository.findByCategory(c2).size();
                    return Long.compare(count2, count1);
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getCategoryTree() {
        List<Category> rootCategories = findByParentCategoryIsNull();
        return buildCategoryTree(rootCategories);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private void validateCategory(Category category) {
        // Check for duplicate name (only for new categories or name changes)
        if (category.getId() == null) {
            // New category
            if (existsByName(category.getName())) {
                throw new RuntimeException("Category with name '" + category.getName() + "' already exists");
            }
        } else {
            // Existing category - check if name changed and is duplicate
            Optional<Category> existing = findByNameIgnoreCase(category.getName());
            if (existing.isPresent() && !existing.get().getId().equals(category.getId())) {
                throw new RuntimeException("Category with name '" + category.getName() + "' already exists");
            }
        }

        // Validate parent category
        if (category.getParentCategory() != null) {
            // Parent must exist
            if (!categoryRepository.existsById(category.getParentCategory().getId())) {
                throw new RuntimeException("Parent category does not exist");
            }

            // Cannot set self as parent
            if (category.getId() != null && category.getId().equals(category.getParentCategory().getId())) {
                throw new RuntimeException("Category cannot be its own parent");
            }

            // Check for circular reference
            if (wouldCreateCircularReference(category)) {
                throw new RuntimeException("Circular reference detected in category hierarchy");
            }
        }
    }

    private boolean wouldCreateCircularReference(Category category) {
        if (category.getParentCategory() == null || category.getId() == null) {
            return false;
        }

        Set<String> visited = new HashSet<>();
        Category current = category.getParentCategory();

        while (current != null) {
            if (visited.contains(current.getId())) {
                return true; // Circular reference detected
            }

            if (current.getId().equals(category.getId())) {
                return true; // Found self in parent chain
            }

            visited.add(current.getId());
            current = current.getParentCategory();
        }

        return false;
    }

    private void collectDescendants(Category parent, List<Category> descendants) {
        List<Category> children = findByParentCategory(parent);
        descendants.addAll(children);

        for (Category child : children) {
            collectDescendants(child, descendants);
        }
    }

    private List<Map<String, Object>> buildCategoryTree(List<Category> categories) {
        List<Map<String, Object>> tree = new ArrayList<>();

        for (Category category : categories) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", category.getId());
            node.put("name", category.getName());
            node.put("description", category.getDescription());
            node.put("iconUrl", category.getIconUrl());
            node.put("displayOrder", category.getDisplayOrder());
            node.put("isActive", category.getIsActive());
            node.put("itemCount", libraryRepository.findByCategory(category).size());

            // Get children
            List<Category> children = findByParentCategory(category);
            if (!children.isEmpty()) {
                node.put("children", buildCategoryTree(children));
            }

            tree.add(node);
        }

        return tree;
    }

    private String getMostPopularCategory() {
        return categoryRepository.findAll().stream()
                .filter(Category::getIsActive)
                .max((c1, c2) -> {
                    long count1 = libraryRepository.findByCategory(c1).size();
                    long count2 = libraryRepository.findByCategory(c2).size();
                    return Long.compare(count1, count2);
                })
                .map(Category::getName)
                .orElse("None");
    }
}