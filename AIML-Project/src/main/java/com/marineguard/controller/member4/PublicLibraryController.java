package com.marineguard.controller.member4;

import com.marineguard.model.member4.LibraryItem;
import com.marineguard.model.member4.Category;
import com.marineguard.service.member4.LibraryService;
import com.marineguard.service.member4.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PublicLibraryController {

    private final LibraryService libraryService;
    private final CategoryService categoryService;

    // ==================== CATEGORY ENDPOINTS ====================

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.findByIsActiveTrue();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/categories/parents")
    public ResponseEntity<List<Category>> getParentCategories() {
        List<Category> parentCategories = categoryService.findByParentCategoryIsNull();
        return ResponseEntity.ok(parentCategories);
    }

    @GetMapping("/categories/{id}/subcategories")
    public ResponseEntity<List<Category>> getSubCategories(@PathVariable String id) {
        Optional<Category> parent = categoryService.findById(id);
        if (parent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Category> subCategories = categoryService.findByParentCategory(parent.get());
        return ResponseEntity.ok(subCategories);
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable String id) {
        Optional<Category> category = categoryService.findById(id);
        return category.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== LIBRARY ITEM ENDPOINTS ====================

    @GetMapping("/items")
    public ResponseEntity<List<LibraryItem>> getPublishedItems(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String search) {

        List<LibraryItem> items;

        if (search != null && !search.isEmpty()) {
            items = libraryService.searchItems(search);
        } else if (category != null) {
            items = libraryService.filterByCategory(category);
        } else if (type != null) {
            try {
                LibraryItem.ItemType itemType = LibraryItem.ItemType.valueOf(type.toUpperCase());
                items = libraryService.filterByType(itemType);
            } catch (IllegalArgumentException e) {
                items = libraryService.getPublishedItems();
            }
        } else if (tag != null) {
            items = libraryService.filterByTags(List.of(tag));
        } else if (difficulty != null) {
            items = libraryService.filterByDifficulty(difficulty);
        } else if (language != null) {
            items = libraryService.filterByLanguage(language);
        } else {
            items = libraryService.getPublishedItems();
        }

        return ResponseEntity.ok(items);
    }

    @GetMapping("/items/featured")
    public ResponseEntity<List<LibraryItem>> getFeaturedItems() {
        List<LibraryItem> featuredItems = libraryService.getFeaturedItems();
        return ResponseEntity.ok(featuredItems);
    }

    @GetMapping("/items/popular")
    public ResponseEntity<List<LibraryItem>> getPopularItems(
            @RequestParam(defaultValue = "10") int limit) {
        List<LibraryItem> popularItems = libraryService.getPopularItems(limit);
        return ResponseEntity.ok(popularItems);
    }

    @GetMapping("/items/recent")
    public ResponseEntity<List<LibraryItem>> getRecentItems(
            @RequestParam(defaultValue = "10") int limit) {
        List<LibraryItem> recentItems = libraryService.getRecentItems(limit);
        return ResponseEntity.ok(recentItems);
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<LibraryItem> getLibraryItemById(@PathVariable String id) {
        Optional<LibraryItem> item = libraryService.getLibraryItemById(id);

        if (item.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Check if item is published
        if (!item.get().getIsPublished()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(item.get());
    }

    @GetMapping("/items/{id}/related")
    public ResponseEntity<List<LibraryItem>> getRelatedItems(
            @PathVariable String id,
            @RequestParam(defaultValue = "5") int limit) {
        List<LibraryItem> relatedItems = libraryService.getRelatedItems(id, limit);
        return ResponseEntity.ok(relatedItems);
    }

    @GetMapping("/items/{id}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String id) {
        Optional<LibraryItem> item = libraryService.getLibraryItemById(id);

        if (item.isEmpty() || !item.get().getIsPublished()) {
            return ResponseEntity.notFound().build();
        }

        LibraryItem libraryItem = item.get();

        if (libraryItem.getFileUrl() == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            byte[] fileContent = libraryService.getFileContent(libraryItem.getFileUrl());

            libraryService.recordDownload(id);

            String filename = libraryItem.getTitle().replaceAll("[^a-zA-Z0-9]", "_");
            String extension = libraryItem.getFileUrl().substring(libraryItem.getFileUrl().lastIndexOf("."));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + extension + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(fileContent);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== INTERACTION ENDPOINTS ====================

    @PostMapping("/items/{id}/view")
    public ResponseEntity<Void> recordView(@PathVariable String id) {
        libraryService.recordView(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/items/{id}/like")
    public ResponseEntity<Map<String, Object>> recordLike(
            @PathVariable String id,
            @RequestParam(required = false) String userId) {
        libraryService.recordLike(id, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Like recorded successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/items/{id}/share")
    public ResponseEntity<Map<String, Object>> recordShare(@PathVariable String id) {
        libraryService.recordShare(id);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Share recorded successfully");

        return ResponseEntity.ok(response);
    }

    // ==================== STATISTICS ENDPOINTS ====================

    @GetMapping("/stats/overview")
    public ResponseEntity<Map<String, Object>> getLibraryStats() {
        Map<String, Object> stats = new HashMap<>();

        List<LibraryItem> publishedItems = libraryService.getPublishedItems();
        List<Category> categories = categoryService.findByIsActiveTrue();

        stats.put("totalItems", publishedItems.size());
        stats.put("totalCategories", categories.size());
        stats.put("totalViews", publishedItems.stream().mapToInt(LibraryItem::getViewCount).sum());
        stats.put("totalDownloads", publishedItems.stream().mapToInt(LibraryItem::getDownloadCount).sum());

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/items/{id}/stats")
    public ResponseEntity<Map<String, Object>> getItemStats(@PathVariable String id) {
        Map<String, Object> stats = libraryService.getItemStatistics(id);
        return ResponseEntity.ok(stats);
    }
}