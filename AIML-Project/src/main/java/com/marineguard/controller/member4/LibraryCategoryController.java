package com.marineguard.controller.member4;

import com.marineguard.model.member4.Category;
import com.marineguard.service.member4.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/library/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN') or hasRole('NGO')")
public class LibraryCategoryController {

    private final CategoryService categoryService;

    // ==================== CREATE ====================

    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Category category,
                                                   @RequestHeader("X-User-ID") String userId) {
        // Set audit fields
        category.setCreatedBy(userId);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedBy(userId);
        category.setUpdatedAt(LocalDateTime.now());
        category.setIsActive(true);

        Category savedCategory = categoryService.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory);
    }

    // ==================== READ ====================

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.findAll();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Category>> getActiveCategories() {
        List<Category> categories = categoryService.findByIsActiveTrue();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/parents")
    public ResponseEntity<List<Category>> getParentCategories() {
        List<Category> categories = categoryService.findByParentCategoryIsNull();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable String id) {
        Optional<Category> category = categoryService.findById(id);
        return category.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/subcategories")
    public ResponseEntity<List<Category>> getSubCategories(@PathVariable String id) {
        Optional<Category> parent = categoryService.findById(id);
        if (parent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Category> subCategories = categoryService.findByParentCategory(parent.get());
        return ResponseEntity.ok(subCategories);
    }

    @GetMapping("/exists")
    public ResponseEntity<Map<String, Boolean>> checkCategoryExists(@RequestParam String name) {
        boolean exists = categoryService.existsByName(name);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    // ==================== UPDATE ====================

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable String id,
                                                   @RequestBody Category categoryDetails,
                                                   @RequestHeader("X-User-ID") String userId) {
        Optional<Category> optionalCategory = categoryService.findById(id);

        if (optionalCategory.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Category category = optionalCategory.get();

        // Update fields
        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        category.setIconUrl(categoryDetails.getIconUrl());
        category.setParentCategory(categoryDetails.getParentCategory());
        category.setTags(categoryDetails.getTags());
        category.setDisplayOrder(categoryDetails.getDisplayOrder());
        category.setUpdatedBy(userId);
        category.setUpdatedAt(LocalDateTime.now());

        Category updatedCategory = categoryService.save(category);
        return ResponseEntity.ok(updatedCategory);
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<Category> toggleCategoryStatus(@PathVariable String id,
                                                         @RequestParam Boolean active,
                                                         @RequestHeader("X-User-ID") String userId) {
        Optional<Category> optionalCategory = categoryService.findById(id);

        if (optionalCategory.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Category category = optionalCategory.get();
        category.setIsActive(active);
        category.setUpdatedBy(userId);
        category.setUpdatedAt(LocalDateTime.now());

        Category updatedCategory = categoryService.save(category);
        return ResponseEntity.ok(updatedCategory);
    }

    @PatchMapping("/{id}/reorder")
    public ResponseEntity<Category> updateDisplayOrder(@PathVariable String id,
                                                       @RequestParam Integer displayOrder,
                                                       @RequestHeader("X-User-ID") String userId) {
        Optional<Category> optionalCategory = categoryService.findById(id);

        if (optionalCategory.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Category category = optionalCategory.get();
        category.setDisplayOrder(displayOrder);
        category.setUpdatedBy(userId);
        category.setUpdatedAt(LocalDateTime.now());

        Category updatedCategory = categoryService.save(category);
        return ResponseEntity.ok(updatedCategory);
    }

    // ==================== DELETE ====================

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteCategory(@PathVariable String id,
                                                              @RequestHeader("X-User-ID") String userId) {
        Optional<Category> optionalCategory = categoryService.findById(id);

        if (optionalCategory.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Check if category has subcategories
        List<Category> subCategories = categoryService.findByParentCategory(optionalCategory.get());
        if (!subCategories.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Cannot delete category with subcategories. Delete subcategories first.");
            return ResponseEntity.badRequest().body(error);
        }

        categoryService.deleteById(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Category deleted successfully");
        response.put("id", id);
        response.put("deletedBy", userId);
        return ResponseEntity.ok(response);
    }

    // ==================== BULK OPERATIONS ====================

    @PostMapping("/bulk")
    public ResponseEntity<List<Category>> bulkCreateCategories(@RequestBody List<Category> categories,
                                                               @RequestHeader("X-User-ID") String userId) {
        for (Category category : categories) {
            category.setCreatedBy(userId);
            category.setCreatedAt(LocalDateTime.now());
            category.setUpdatedBy(userId);
            category.setUpdatedAt(LocalDateTime.now());
            category.setIsActive(true);
        }

        List<Category> savedCategories = categoryService.saveAll(categories);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCategories);
    }

    @DeleteMapping("/bulk")
    public ResponseEntity<Map<String, Object>> bulkDeleteCategories(@RequestBody List<String> ids,
                                                                    @RequestHeader("X-User-ID") String userId) {
        List<Category> categories = categoryService.findAllById(ids);
        List<String> deletedIds = new java.util.ArrayList<>();
        List<String> failedIds = new java.util.ArrayList<>();

        for (Category category : categories) {
            // Check if category has subcategories
            List<Category> subCategories = categoryService.findByParentCategory(category);
            if (subCategories.isEmpty()) {
                categoryService.deleteById(category.getId());
                deletedIds.add(category.getId());
            } else {
                failedIds.add(category.getId());
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Bulk delete completed");
        response.put("deletedCount", deletedIds.size());
        response.put("deletedIds", deletedIds);
        response.put("failedCount", failedIds.size());
        response.put("failedIds", failedIds);
        response.put("deletedBy", userId);

        return ResponseEntity.ok(response);
    }
}