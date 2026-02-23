package com.marineguard.service.member4;

import com.marineguard.model.member4.LibraryItem;
import com.marineguard.model.member4.Category;
import com.marineguard.repository.member4.LibraryRepository;
import com.marineguard.repository.member4.LibraryRepositoryCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LibraryServiceImpl implements LibraryService {

    private final LibraryRepository libraryRepository;
    private final CategoryService categoryService;  // You'll need to create this
    private final FileStorageService fileStorageService;

    private final String UPLOAD_DIR = "uploads/library/";

    // ==================== CATEGORY OPERATIONS ====================

    @Override
    public Category createCategory(Category category, String userId) {
        log.info("Creating new category: {} by user: {}", category.getName(), userId);

        // Check if category with same name exists
        if (categoryService.existsByName(category.getName())) {
            throw new RuntimeException("Category with name '" + category.getName() + "' already exists");
        }

        category.setCreatedBy(userId);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedBy(userId);
        category.setUpdatedAt(LocalDateTime.now());
        category.setIsActive(true);

        return categoryService.save(category);
    }

    @Override
    public Optional<Category> getCategoryById(String id) {
        return categoryService.findById(id);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryService.findAll();
    }

    @Override
    public List<Category> getActiveCategories() {
        return categoryService.findByIsActiveTrue();
    }

    @Override
    public List<Category> getParentCategories() {
        return categoryService.findByParentCategoryIsNull();
    }

    @Override
    public List<Category> getSubCategories(String parentId) {
        Optional<Category> parent = categoryService.findById(parentId);
        return parent.map(categoryService::findByParentCategory)
                .orElse(Collections.emptyList());
    }

    @Override
    public Category updateCategory(String id, Category categoryDetails, String userId) {
        log.info("Updating category: {} by user: {}", id, userId);

        Category category = categoryService.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Update fields
        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        category.setIconUrl(categoryDetails.getIconUrl());
        category.setTags(categoryDetails.getTags());
        category.setDisplayOrder(categoryDetails.getDisplayOrder());
        category.setUpdatedBy(userId);
        category.setUpdatedAt(LocalDateTime.now());

        return categoryService.save(category);
    }

    @Override
    public void deleteCategory(String id, String userId) {
        log.info("Deleting category: {} by user: {}", id, userId);

        // Check if category has items
        List<LibraryItem> items = libraryRepository.findByCategoryId(id);
        if (!items.isEmpty()) {
            throw new RuntimeException("Cannot delete category with existing library items. Move items first.");
        }

        categoryService.deleteById(id);
    }

    @Override
    public void toggleCategoryStatus(String id, Boolean isActive, String userId) {
        Category category = categoryService.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        category.setIsActive(isActive);
        category.setUpdatedBy(userId);
        category.setUpdatedAt(LocalDateTime.now());

        categoryService.save(category);
    }

    // ==================== LIBRARY ITEM OPERATIONS ====================

    @Override
    public LibraryItem createLibraryItem(LibraryItem libraryItem, MultipartFile file, String userId) {
        log.info("Creating new library item: {} by user: {}", libraryItem.getTitle(), userId);

        // Set audit fields
        libraryItem.setCreatedBy(userId);
        libraryItem.setCreatedAt(LocalDateTime.now());
        libraryItem.setUpdatedBy(userId);
        libraryItem.setUpdatedAt(LocalDateTime.now());
        libraryItem.setViewCount(0);
        libraryItem.setDownloadCount(0);
        libraryItem.setLikeCount(0);
        libraryItem.setShareCount(0);
        libraryItem.setIsPublished(false);
        libraryItem.setIsFeatured(false);

        // Handle file upload if present
        if (file != null && !file.isEmpty()) {
            String fileUrl = storeFile(file, null);
            libraryItem.setFileUrl(fileUrl);

            // Set thumbnail if not provided
            if (libraryItem.getThumbnailUrl() == null) {
                libraryItem.setThumbnailUrl(generateThumbnail(fileUrl));
            }
        }

        return libraryRepository.save(libraryItem);
    }

    @Override
    public LibraryItem createLibraryItemFromText(LibraryItem libraryItem, String userId) {
        log.info("Creating new text-based library item: {} by user: {}", libraryItem.getTitle(), userId);

        libraryItem.setCreatedBy(userId);
        libraryItem.setCreatedAt(LocalDateTime.now());
        libraryItem.setUpdatedBy(userId);
        libraryItem.setUpdatedAt(LocalDateTime.now());
        libraryItem.setViewCount(0);
        libraryItem.setDownloadCount(0);
        libraryItem.setLikeCount(0);
        libraryItem.setShareCount(0);
        libraryItem.setIsPublished(false);
        libraryItem.setIsFeatured(false);

        return libraryRepository.save(libraryItem);
    }

    @Override
    public Optional<LibraryItem> getLibraryItemById(String id) {
        Optional<LibraryItem> item = libraryRepository.findById(id);

        // Increment view count when accessed
        item.ifPresent(libraryItem -> {
            libraryItem.incrementViewCount();
            libraryRepository.save(libraryItem);
        });

        return item;
    }

    @Override
    public List<LibraryItem> getAllLibraryItems() {
        return libraryRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Override
    public List<LibraryItem> getPublishedItems() {
        return libraryRepository.findByIsPublishedTrue();
    }

    @Override
    public List<LibraryItem> getFeaturedItems() {
        return libraryRepository.findByIsFeaturedTrueAndIsPublishedTrue();
    }

    @Override
    public LibraryItem updateLibraryItem(String id, LibraryItem itemDetails, MultipartFile file, String userId) {
        log.info("Updating library item: {} by user: {}", id, userId);

        LibraryItem item = libraryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Library item not found with id: " + id));

        // Update fields
        item.setTitle(itemDetails.getTitle());
        item.setDescription(itemDetails.getDescription());
        item.setType(itemDetails.getType());
        item.setCategory(itemDetails.getCategory());
        item.setTags(itemDetails.getTags());
        item.setAuthor(itemDetails.getAuthor());
        item.setSource(itemDetails.getSource());
        item.setDifficulty(itemDetails.getDifficulty());
        item.setLanguage(itemDetails.getLanguage());
        item.setReadTimeMinutes(itemDetails.getReadTimeMinutes());

        // Update content if provided
        if (itemDetails.getContent() != null) {
            item.setContent(itemDetails.getContent());
        }

        // Handle new file upload
        if (file != null && !file.isEmpty()) {
            // Delete old file
            if (item.getFileUrl() != null) {
                deleteFile(item.getFileUrl());
            }

            String fileUrl = storeFile(file, id);
            item.setFileUrl(fileUrl);
        }

        // Update metadata
        if (itemDetails.getMetadata() != null) {
            item.setMetadata(itemDetails.getMetadata());
        }

        item.setUpdatedBy(userId);
        item.setUpdatedAt(LocalDateTime.now());

        return libraryRepository.save(item);
    }

    @Override
    public LibraryItem updateLibraryItemMetadata(String id, Map<String, Object> metadata, String userId) {
        LibraryItem item = libraryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Library item not found with id: " + id));

        if (item.getMetadata() == null) {
            item.setMetadata(new HashMap<>());
        }

        item.getMetadata().putAll(metadata);
        item.setUpdatedBy(userId);
        item.setUpdatedAt(LocalDateTime.now());

        return libraryRepository.save(item);
    }

    @Override
    public void deleteLibraryItem(String id, String userId) {
        log.info("Deleting library item: {} by user: {}", id, userId);

        LibraryItem item = libraryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Library item not found with id: " + id));

        // Delete associated file
        if (item.getFileUrl() != null) {
            deleteFile(item.getFileUrl());
        }

        libraryRepository.deleteById(id);
    }

    @Override
    public void publishLibraryItem(String id, String reviewerId) {
        log.info("Publishing library item: {} by reviewer: {}", id, reviewerId);

        LibraryItem item = libraryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Library item not found with id: " + id));

        item.publish(reviewerId);
        libraryRepository.save(item);
    }

    @Override
    public void unpublishLibraryItem(String id, String userId) {
        LibraryItem item = libraryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Library item not found with id: " + id));

        item.unpublish();
        item.setUpdatedBy(userId);
        item.setUpdatedAt(LocalDateTime.now());

        libraryRepository.save(item);
    }

    // ==================== SEARCH AND FILTER ====================

    @Override
    public List<LibraryItem> searchItems(String keyword) {
        log.info("Searching library items with keyword: {}", keyword);
        return libraryRepository.fullTextSearch(keyword);
    }

    @Override
    public List<LibraryItem> filterByCategory(String categoryId) {
        return libraryRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<LibraryItem> filterByType(LibraryItem.ItemType type) {
        return libraryRepository.findByType(type);
    }

    @Override
    public List<LibraryItem> filterByTags(List<String> tags) {
        if (tags.size() == 1) {
            return libraryRepository.findByTag(tags.get(0));
        }
        return libraryRepository.findByTags(tags);
    }

    @Override
    public List<LibraryItem> filterByDifficulty(Integer difficulty) {
        return libraryRepository.findByDifficulty(difficulty);
    }

    @Override
    public List<LibraryItem> filterByLanguage(String language) {
        return libraryRepository.findByLanguage(language);
    }

    @Override
    public List<LibraryItem> advancedSearch(Map<String, Object> filters) {
        // Implementation for advanced search with multiple filters
        // This would use a custom repository method
        return libraryRepository.findAll(); // Placeholder
    }

    // ==================== STATISTICS AND TRACKING ====================

    @Override
    public void recordView(String itemId) {
        libraryRepository.findById(itemId).ifPresent(item -> {
            item.incrementViewCount();
            libraryRepository.save(item);
        });
    }

    @Override
    public void recordDownload(String itemId) {
        libraryRepository.findById(itemId).ifPresent(item -> {
            item.incrementDownloadCount();
            libraryRepository.save(item);
        });
    }

    @Override
    public void recordLike(String itemId, String userId) {
        libraryRepository.findById(itemId).ifPresent(item -> {
            item.incrementLikeCount();
            libraryRepository.save(item);
        });
    }

    @Override
    public void recordShare(String itemId) {
        libraryRepository.findById(itemId).ifPresent(item -> {
            item.incrementShareCount();
            libraryRepository.save(item);
        });
    }

    @Override
    public Map<String, Object> getItemStatistics(String itemId) {
        LibraryItem item = libraryRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        Map<String, Object> stats = new HashMap<>();
        stats.put("views", item.getViewCount());
        stats.put("downloads", item.getDownloadCount());
        stats.put("likes", item.getLikeCount());
        stats.put("shares", item.getShareCount());

        return stats;
    }

    @Override
    public Map<String, Object> getCategoryStatistics() {
        Map<String, Object> stats = new HashMap<>();

        List<Category> categories = categoryService.findAll();
        Map<String, Long> itemCountByCategory = new HashMap<>();

        for (Category category : categories) {
            long count = libraryRepository.findByCategory(category).size();
            itemCountByCategory.put(category.getName(), count);
        }

        stats.put("totalCategories", categories.size());
        stats.put("itemCountByCategory", itemCountByCategory);

        return stats;
    }

    // ==================== RECOMMENDATIONS ====================

    @Override
    public List<LibraryItem> getRelatedItems(String itemId, int limit) {
        Optional<LibraryItem> item = libraryRepository.findById(itemId);

        if (item.isEmpty()) {
            return Collections.emptyList();
        }

        // Find items with same category or tags
        List<String> tags = item.get().getTags();
        Category category = item.get().getCategory();

        Pageable pageable = PageRequest.of(0, limit);

        // Custom query to find related items
        return libraryRepository.findByCategoryAndTagsNotSelf(category, tags, itemId, pageable);
    }

    @Override
    public List<LibraryItem> getRecommendedForUser(String userId, int limit) {
        // This would use user's history and preferences
        // Placeholder implementation - return popular items
        return getPopularItems(limit);
    }

    @Override
    public List<LibraryItem> getPopularItems(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return libraryRepository.findTop10ByIsPublishedTrueOrderByViewCountDesc();
    }

    @Override
    public List<LibraryItem> getRecentItems(int limit) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return libraryRepository.findRecentItems(thirtyDaysAgo);
    }

    // ==================== FILE HANDLING ====================

    @Override
    public String storeFile(MultipartFile file, String itemId) {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;

            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return relative path for database storage
            return UPLOAD_DIR + filename;

        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new RuntimeException("Failed to store file: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            Path filePath = Paths.get(fileUrl);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileUrl, e);
        }
    }

    @Override
    public byte[] getFileContent(String fileUrl) {
        try {
            Path filePath = Paths.get(fileUrl);
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Failed to read file: {}", fileUrl, e);
            throw new RuntimeException("Failed to read file: " + e.getMessage());
        }
    }

    private String generateThumbnail(String fileUrl) {
        // Generate thumbnail for images/videos
        // Placeholder implementation
        return fileUrl.replace(".", "_thumb.");
    }

    // ==================== BULK OPERATIONS ====================

    @Override
    public List<LibraryItem> bulkUpload(List<MultipartFile> files, Map<String, Object> metadata, String userId) {
        List<LibraryItem> createdItems = new ArrayList<>();

        for (MultipartFile file : files) {
            LibraryItem item = new LibraryItem();
            item.setTitle((String) metadata.getOrDefault("title", file.getOriginalFilename()));
            item.setDescription((String) metadata.get("description"));
            item.setType(LibraryItem.ItemType.valueOf((String) metadata.getOrDefault("type", "PDF")));

            // Set category from metadata
            String categoryId = (String) metadata.get("categoryId");
            categoryService.findById(categoryId).ifPresent(item::setCategory);

            createdItems.add(createLibraryItem(item, file, userId));
        }

        return createdItems;
    }

    @Override
    public void bulkDelete(List<String> itemIds, String userId) {
        for (String id : itemIds) {
            deleteLibraryItem(id, userId);
        }
    }

    @Override
    public void bulkPublish(List<String> itemIds, String reviewerId) {
        for (String id : itemIds) {
            publishLibraryItem(id, reviewerId);
        }
    }
}