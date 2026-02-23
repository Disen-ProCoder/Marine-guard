package com.marineguard.repository.member4;

import com.marineguard.model.member4.LibraryItem;
import com.marineguard.model.member4.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibraryRepository extends MongoRepository<LibraryItem, String> {

    // Find by category
    List<LibraryItem> findByCategory(Category category);

    // Find by category ID
    @Query("{ 'category.$id' : ?0 }")
    List<LibraryItem> findByCategoryId(String categoryId);

    // Find by type
    List<LibraryItem> findByType(LibraryItem.ItemType type);

    // Find published items
    List<LibraryItem> findByIsPublishedTrue();

    // Find featured items
    List<LibraryItem> findByIsFeaturedTrueAndIsPublishedTrue();

    // Search by title (case-insensitive)
    @Query("{ 'title': { $regex: ?0, $options: 'i' } }")
    List<LibraryItem> searchByTitle(String title);

    // Search by tags
    @Query("{ 'tags': { $in: [?0] } }")
    List<LibraryItem> findByTag(String tag);

    // Search by multiple tags
    @Query("{ 'tags': { $all: ?0 } }")
    List<LibraryItem> findByTags(List<String> tags);

    // Full-text search across title, description, tags
    @Query("{ $text: { $search: ?0 } }")
    List<LibraryItem> fullTextSearch(String keyword);

    // Find by difficulty level
    List<LibraryItem> findByDifficulty(Integer difficulty);

    // Find by language
    List<LibraryItem> findByLanguage(String language);

    // Find by author
    List<LibraryItem> findByAuthor(String author);

    // Find recent items (last 30 days)
    @Query("{ 'createdAt': { $gte: ?0 } }")
    List<LibraryItem> findRecentItems(java.time.LocalDateTime since);

    // Find most viewed
    List<LibraryItem> findTop10ByIsPublishedTrueOrderByViewCountDesc();

    // Find most liked
    List<LibraryItem> findTop10ByIsPublishedTrueOrderByLikeCountDesc();

    // Find by multiple filters
    @Query("{ 'isPublished': ?0, 'category.$id': ?1, 'type': ?2 }")
    List<LibraryItem> findByFilters(Boolean isPublished, String categoryId, LibraryItem.ItemType type);

    // Find by created by user
    List<LibraryItem> findByCreatedBy(String userId);

    // Check if exists by title
    boolean existsByTitle(String title);

    // Delete by category
    void deleteByCategory(Category category);
}