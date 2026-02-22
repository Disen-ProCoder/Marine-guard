package com.marineguard.model.member2;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "observations")
public class Observation {

    @Id
    private String id;

    private String userId;
    private String imageUrl;
    private String description;
    private String location;
    private Double latitude;
    private Double longitude;

    @Builder.Default
    private ObservationStatus status = ObservationStatus.PENDING;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @DBRef
    private AIPredictionResult aiResult;

    private List<String> tags;
    private Boolean isPublic;
    private Integer severityScore; // 0-4 scale

    // Getters and Setters (Lombok generates these)

    public enum ObservationStatus {
        PENDING,        // Waiting for AI analysis
        PROCESSING,     // AI analysis in progress
        COMPLETED,      // AI analysis done
        DRAFT,          // Saved as draft
        WITHDRAWN,      // User withdrew
        REJECTED        // Admin rejected
    }
}