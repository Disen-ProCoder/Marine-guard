package com.marineguard.service.member4;

import com.marineguard.model.member4.LibraryItem;
import com.marineguard.model.member4.Category;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LibraryService {

    // Category CRUD operations
    Category createCategory(Category category, String userId);
    Optional<Category> getCategoryById(String id);
    List<Category> getAllCategories();
    List<Category> getActiveCategories();
    List<Category> getParentCategories();
    List<Category> getSubCategories(String parentId);
    Category updateCategory(String id, Category categoryDetails, String userId);
    void deleteCategory(String id, String userId);
    void toggleCategoryStatus(String id, Boolean isActive, String userId);

    // Library Item CRUD operations
    LibraryItem createLibraryItem(LibraryItem libraryItem, MultipartFile file, String userId);
    LibraryItem createLibraryItemFromText(LibraryItem libraryItem, String userId);
    Optional<LibraryItem> getLibraryItemById(String id);
    List<LibraryItem> getAllLibraryItems();
    List<LibraryItem> getPublishedItems();
    List<LibraryItem> getFeaturedItems();
    LibraryItem updateLibraryItem(String id, LibraryItem itemDetails, MultipartFile file, String userId);
    LibraryItem updateLibraryItemMetadata(String id, Map<String, Object> metadata, String userId);
    void deleteLibraryItem(String id, String userId);
    void publishLibraryItem(String id, String reviewerId);
    void unpublishLibraryItem(String id, String userId);

    // Search and filter
    List<LibraryItem> searchItems(String keyword);
    List<LibraryItem> filterByCategory(String categoryId);
    List<LibraryItem> filterByType(LibraryItem.ItemType type);
    List<LibraryItem> filterByTags(List<String> tags);
    List<LibraryItem> filterByDifficulty(Integer difficulty);
    List<LibraryItem> filterByLanguage(String language);
    List<LibraryItem> advancedSearch(Map<String, Object> filters);

    // Statistics and tracking
    void recordView(String itemId);
    void recordDownload(String itemId);
    void recordLike(String itemId, String userId);
    void recordShare(String itemId);
    Map<String, Object> getItemStatistics(String itemId);
    Map<String, Object> getCategoryStatistics();

    // Bulk operations
    List<LibraryItem> bulkUpload(List<MultipartFile> files, Map<String, Object> metadata, String userId);
    void bulkDelete(List<String> itemIds, String userId);
    void bulkPublish(List<String> itemIds, String reviewerId);

    // Recommendations
    List<LibraryItem> getRelatedItems(String itemId, int limit);
    List<LibraryItem> getRecommendedForUser(String userId, int limit);
    List<LibraryItem> getPopularItems(int limit);
    List<LibraryItem> getRecentItems(int limit);

    // File handling
    String storeFile(MultipartFile file, String itemId);
    void deleteFile(String fileUrl);
    byte[] getFileContent(String fileUrl);
}