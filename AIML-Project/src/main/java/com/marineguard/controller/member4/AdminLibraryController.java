package com.marineguard.controller.member4;

import com.marineguard.model.member4.LibraryItem;
import com.marineguard.model.member4.Category;
import com.marineguard.service.member4.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/library")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN') or hasRole('NGO')")
public class AdminLibraryController {

    private final LibraryService libraryService;

    // ==================== CATEGORY MANAGEMENT ====================

    @PostMapping("/categories")
    public ResponseEntity<Category> createCategory(
            @RequestBody Category category,
            @RequestHeader("X-User-ID") String userId) {

        Category createdCategory = libraryService.createCategory(category, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable String id,
            @RequestBody Category category,
            @RequestHeader("X-User-ID") String userId) {

        Category updatedCategory = libraryService.updateCategory(id, category, userId);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Map<String, String>> deleteCategory(
            @PathVariable String id,
            @RequestHeader("X-User-ID") String userId) {

        libraryService.deleteCategory(id, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Category deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/categories/{id}/toggle-status")
    public ResponseEntity<Map<String, String>> toggleCategoryStatus(
            @PathVariable String id,
            @RequestParam Boolean active,
            @RequestHeader("X-User-ID") String userId) {

        libraryService.toggleCategoryStatus(id, active, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Category status updated to: " + active);
        return ResponseEntity.ok(response);
    }

    // ==================== LIBRARY ITEM MANAGEMENT ====================

    @PostMapping("/items")
    public ResponseEntity<LibraryItem> createLibraryItem(
            @RequestPart("item") LibraryItem libraryItem,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestHeader("X-User-ID") String userId) {

        LibraryItem createdItem;

        if (file != null && !file.isEmpty()) {
            createdItem = libraryService.createLibraryItem(libraryItem, file, userId);
        } else {
            createdItem = libraryService.createLibraryItemFromText(libraryItem, userId);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    @GetMapping("/items")
    public ResponseEntity<List<LibraryItem>> getAllLibraryItems() {
        List<LibraryItem> items = libraryService.getAllLibraryItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/items/pending")
    public ResponseEntity<List<LibraryItem>> getPendingItems() {
        // Custom method to get unpublished items
        List<LibraryItem> allItems = libraryService.getAllLibraryItems();
        List<LibraryItem> pendingItems = allItems.stream()
                .filter(item -> !item.getIsPublished())
                .toList();
        return ResponseEntity.ok(pendingItems);
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<LibraryItem> getLibraryItemById(@PathVariable String id) {
        Optional<LibraryItem> item = libraryService.getLibraryItemById(id);
        return item.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<LibraryItem> updateLibraryItem(
            @PathVariable String id,
            @RequestPart("item") LibraryItem libraryItem,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestHeader("X-User-ID") String userId) {

        LibraryItem updatedItem = libraryService.updateLibraryItem(id, libraryItem, file, userId);
        return ResponseEntity.ok(updatedItem);
    }

    @PatchMapping("/items/{id}/metadata")
    public ResponseEntity<LibraryItem> updateMetadata(
            @PathVariable String id,
            @RequestBody Map<String, Object> metadata,
            @RequestHeader("X-User-ID") String userId) {

        LibraryItem updatedItem = libraryService.updateLibraryItemMetadata(id, metadata, userId);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Map<String, String>> deleteLibraryItem(
            @PathVariable String id,
            @RequestHeader("X-User-ID") String userId) {

        libraryService.deleteLibraryItem(id, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Library item deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items/{id}/publish")
    public ResponseEntity<Map<String, String>> publishItem(
            @PathVariable String id,
            @RequestHeader("X-User-ID") String reviewerId) {

        libraryService.publishLibraryItem(id, reviewerId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Item published successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items/{id}/unpublish")
    public ResponseEntity<Map<String, String>> unpublishItem(
            @PathVariable String id,
            @RequestHeader("X-User-ID") String userId) {

        libraryService.unpublishLibraryItem(id, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Item unpublished successfully");
        return ResponseEntity.ok(response);
    }

    // ==================== BULK OPERATIONS ====================

    @PostMapping("/items/bulk-upload")
    public ResponseEntity<List<LibraryItem>> bulkUpload(
            @RequestPart("files") List<MultipartFile> files,
            @RequestPart("metadata") Map<String, Object> metadata,
            @RequestHeader("X-User-ID") String userId) {

        List<LibraryItem> createdItems = libraryService.bulkUpload(files, metadata, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItems);
    }

    @DeleteMapping("/items/bulk-delete")
    public ResponseEntity<Map<String, String>> bulkDelete(
            @RequestBody List<String> itemIds,
            @RequestHeader("X-User-ID") String userId) {

        libraryService.bulkDelete(itemIds, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Items deleted successfully: " + itemIds.size());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items/bulk-publish")
    public ResponseEntity<Map<String, String>> bulkPublish(
            @RequestBody List<String> itemIds,
            @RequestHeader("X-User-ID") String reviewerId) {

        libraryService.bulkPublish(itemIds, reviewerId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Items published successfully: " + itemIds.size());
        return ResponseEntity.ok(response);
    }

    // ==================== STATISTICS ====================

    @GetMapping("/stats/categories")
    public ResponseEntity<Map<String, Object>> getCategoryStatistics() {
        Map<String, Object> stats = libraryService.getCategoryStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/items/{id}")
    public ResponseEntity<Map<String, Object>> getItemStatistics(@PathVariable String id) {
        Map<String, Object> stats = libraryService.getItemStatistics(id);
        return ResponseEntity.ok(stats);
    }
}