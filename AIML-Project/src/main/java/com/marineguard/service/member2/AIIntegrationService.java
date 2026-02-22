package com.marineguard.service.member2;

import com.marineguard.model.member2.AIPredictionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIIntegrationService {

    private final RestTemplate restTemplate;

    @Value("${ai.service.url:http://localhost:5000}")
    private String aiServiceUrl;

    public AIPredictionResult predictCoralHealth(String imagePath) {
        try {
            log.info("Calling AI service for image: {}", imagePath);

            // Prepare request
            Map<String, String> request = new HashMap<>();
            request.put("image_path", imagePath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            // Call Python AI service
            long startTime = System.currentTimeMillis();
            ResponseEntity<Map> response = restTemplate.exchange(
                    aiServiceUrl + "/predict",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );
            long endTime = System.currentTimeMillis();

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> result = response.getBody();

                // Parse response
                return AIPredictionResult.builder()
                        .label((String) result.get("label"))
                        .confidenceScore((Double) result.get("confidence"))
                        .severityLevel((Integer) result.get("severity"))
                        .bleachPercentage((Double) result.get("bleach_percentage"))
                        .classProbabilities((Map<String, Double>) result.get("probabilities"))
                        .heatmapImageUrl((String) result.get("heatmap_url"))
                        .processedAt(LocalDateTime.now())
                        .modelVersion((String) result.get("model_version"))
                        .processingTimeMs((int) (endTime - startTime))
                        .isHighConfidence((Double) result.get("confidence") > 0.8)
                        .build();
            } else {
                throw new RuntimeException("AI service returned error: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error calling AI service: {}", e.getMessage());
            throw new RuntimeException("AI prediction failed: " + e.getMessage());
        }
    }

    public Map<String, Object> getModelInfo() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    aiServiceUrl + "/info",
                    Map.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Error getting model info: {}", e.getMessage());
            return Map.of("error", "AI service unavailable");
        }
    }
}