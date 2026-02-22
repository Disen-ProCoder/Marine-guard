package com.marineguard.model.member2;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ai_predictions")
public class AIPredictionResult {

    @Id
    private String id;

    private String observationId;
    private String label;           // "healthy", "early_bleaching", "moderate_bleaching", "severe_bleaching", "diseased"
    private Double confidenceScore;  // 0.0 to 1.0
    private Integer severityLevel;   // 0-4 scale
    private Double bleachPercentage; // Estimated % of bleaching
    private Map<String, Double> classProbabilities; // All class probabilities

    private LocalDateTime processedAt;
    private String modelVersion;
    private Integer processingTimeMs;

    // Grad-CAM visualization
    private String heatmapImageUrl;  // URL to generated heatmap

    private Boolean isHighConfidence; // confidenceScore > 0.8

    // Getters and Setters (Lombok generates these)
}