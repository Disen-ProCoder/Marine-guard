package com.marineguard.model.member4;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "library_items")
public class LibraryItem {

    @Id
    private String id;

    private String title;               // Title of the content

    private String description;          // Short description

    private ItemType type;               // PDF, ARTICLE, VIDEO, IMAGE_GALLERY, INFOGRAPHIC

    private String content;               // For ARTICLE: HTML/text content

    private String fileUrl;               // For PDF/VIDEO/IMAGE: URL to stored file

    private String thumbnailUrl;          // Thumbnail image URL

    @DBRef
    private Category category;            // Category reference

    private List<String> tags;            // Search tags

    private String author;                // Author name/organization

    private String source;                 // Source of information (e.g., "NOAA", "EFL")

    private Integer readTimeMinutes;       // Estimated read time (for articles)

    private String language = "en";        // Content language

    private Integer difficulty;             // 1-Beginner, 2-Intermediate, 3-Advanced

    private Map<String, Object> metadata;   // Additional flexible fields

    // Statistics
    private Integer viewCount = 0;
    private Integer downloadCount = 0;
    private Integer likeCount = 0;
    private Integer shareCount = 0;

    // Status fields
    private Boolean isPublished = false;    // Whether visible to public
    private Boolean isFeatured = false;     // Whether to show in featured section
    private LocalDateTime publishDate;      // When it was published

    // Audit fields
    private String createdBy;                // User ID who uploaded
    private LocalDateTime createdAt;         // Upload timestamp
    private String updatedBy;                 // User ID who last updated
    private LocalDateTime updatedAt;          // Last update timestamp
    private String reviewedBy;                // User ID who reviewed
    private LocalDateTime reviewedAt;         // Review timestamp

    public enum ItemType {
        PDF("PDF Document"),
        ARTICLE("Article"),
        VIDEO("Video"),
        IMAGE_GALLERY("Image Gallery"),
        INFOGRAPHIC("Infographic"),
        GUIDE("Guide"),
        RESEARCH_PAPER("Research Paper"),
        FAQ("Frequently Asked Questions");

        private final String displayName;

        ItemType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructor for basic item creation
    public LibraryItem(String title, String description, ItemType type,
                       Category category, List<String> tags, String createdBy) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.category = category;
        this.tags = tags;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.updatedBy = createdBy;
        this.updatedAt = LocalDateTime.now();
        this.viewCount = 0;
        this.downloadCount = 0;
        this.likeCount = 0;
        this.shareCount = 0;
        this.isPublished = false;
        this.isFeatured = false;
        this.language = "en";
    }

    // Method to increment view count
    public void incrementViewCount() {
        this.viewCount++;
    }

    // Method to increment download count
    public void incrementDownloadCount() {
        this.downloadCount++;
    }

    // Method to increment like count
    public void incrementLikeCount() {
        this.likeCount++;
    }

    // Method to publish item
    public void publish(String reviewedBy) {
        this.isPublished = true;
        this.publishDate = LocalDateTime.now();
        this.reviewedBy = reviewedBy;
        this.reviewedAt = LocalDateTime.now();
    }

    // Method to unpublish item
    public void unpublish() {
        this.isPublished = false;
    }
}