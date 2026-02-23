package com.marineguard.model.member4;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "categories")
public class Category {

    @Id
    private String id;

    private String name;              // e.g., "Coral Bleaching", "Pollution", "Conservation"

    private String description;        // Description of the category

    private String iconUrl;            // Icon/image for the category

    @DBRef
    private Category parentCategory;   // For sub-categories (nullable)

    private List<String> tags;         // Related search tags

    private Integer displayOrder;      // Order to display in UI

    private Boolean isActive = true;   // Whether category is active

    // Audit fields
    private String createdBy;           // User ID who created
    private LocalDateTime createdAt;    // Creation timestamp
    private String updatedBy;           // User ID who last updated
    private LocalDateTime updatedAt;    // Last update timestamp

    // Constructor without parent
    public Category(String name, String description, String iconUrl,
                    List<String> tags, Integer displayOrder, String createdBy) {
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
        this.tags = tags;
        this.displayOrder = displayOrder;
        this.isActive = true;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.updatedBy = createdBy;
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor with parent
    public Category(String name, String description, String iconUrl,
                    Category parentCategory, List<String> tags,
                    Integer displayOrder, String createdBy) {
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
        this.parentCategory = parentCategory;
        this.tags = tags;
        this.displayOrder = displayOrder;
        this.isActive = true;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.updatedBy = createdBy;
        this.updatedAt = LocalDateTime.now();
    }
}