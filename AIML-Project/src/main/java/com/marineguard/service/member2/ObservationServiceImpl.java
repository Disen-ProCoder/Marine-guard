package com.marineguard.service.member2;

import com.marineguard.model.member2.Observation;
import com.marineguard.model.member2.AIPredictionResult;
import com.marineguard.repository.member2.ObservationRepository;
import com.marineguard.service.member2.AIIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ObservationServiceImpl implements ObservationService {

    private final ObservationRepository observationRepository;
    private final AIIntegrationService aiIntegrationService;

    // Configure upload directory
    private final String UPLOAD_DIR = "uploads/observations/";

    @Override
    public Observation submitObservation(String userId, MultipartFile image,
                                         String description, String location,
                                         Double latitude, Double longitude) {
        try {
            log.info("Submitting new observation for user: {}", userId);

            // 1. Save image to local storage
            String imageUrl = saveImageToStorage(image);

            // 2. Create observation entity
            Observation observation = Observation.builder()
                    .userId(userId)
                    .imageUrl(imageUrl)
                    .description(description)
                    .location(location)
                    .latitude(latitude)
                    .longitude(longitude)
                    .status(Observation.ObservationStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .isPublic(true)
                    .build();

            // 3. Save to database
            Observation savedObservation = observationRepository.save(observation);

            // 4. Trigger AI processing asynchronously
            processWithAIAsync(savedObservation.getId());

            log.info("Observation submitted successfully with ID: {}", savedObservation.getId());
            return savedObservation;

        } catch (Exception e) {
            log.error("Error submitting observation: {}", e.getMessage());
            throw new RuntimeException("Failed to submit observation: " + e.getMessage());
        }
    }

    @Override
    public Observation saveDraft(String userId, MultipartFile image, String description,
                                 String location, Double latitude, Double longitude) {
        try {
            String imageUrl = image != null ? saveImageToStorage(image) : null;

            Observation draft = Observation.builder()
                    .userId(userId)
                    .imageUrl(imageUrl)
                    .description(description)
                    .location(location)
                    .latitude(latitude)
                    .longitude(longitude)
                    .status(Observation.ObservationStatus.DRAFT)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .isPublic(false)
                    .build();

            return observationRepository.save(draft);

        } catch (Exception e) {
            log.error("Error saving draft: {}", e.getMessage());
            throw new RuntimeException("Failed to save draft: " + e.getMessage());
        }
    }

    @Override
    public List<Observation> getUserObservations(String userId) {
        log.info("Fetching observations for user: {}", userId);
        return observationRepository.findByUserId(userId);
    }

    @Override
    public Optional<Observation> getObservationById(String observationId, String userId) {
        Optional<Observation> observation = observationRepository.findById(observationId);

        // Verify user owns this observation or is admin
        if (observation.isPresent() && !observation.get().getUserId().equals(userId)) {
            log.warn("User {} attempted to access observation {} belonging to another user",
                    userId, observationId);
            return Optional.empty();
        }

        return observation;
    }

    @Override
    public List<Observation> getUserObservationsByStatus(String userId, Observation.ObservationStatus status) {
        return observationRepository.findByUserIdAndStatus(userId, status);
    }

    @Override
    public List<Observation> getHighSeverityObservations() {
        return observationRepository.findHighSeverityObservations();
    }

    @Override
    public Observation updateDraft(String observationId, String userId,
                                   MultipartFile image, String description, String location) {
        Observation existing = observationRepository.findById(observationId)
                .orElseThrow(() -> new RuntimeException("Observation not found"));

        if (!existing.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to update this observation");
        }

        if (existing.getStatus() != Observation.ObservationStatus.DRAFT) {
            throw new RuntimeException("Only drafts can be updated");
        }

        try {
            if (image != null && !image.isEmpty()) {
                // Delete old image if exists
                if (existing.getImageUrl() != null) {
                    deleteImageFromStorage(existing.getImageUrl());
                }
                String newImageUrl = saveImageToStorage(image);
                existing.setImageUrl(newImageUrl);
            }

            if (description != null) existing.setDescription(description);
            if (location != null) existing.setLocation(location);

            existing.setUpdatedAt(LocalDateTime.now());

            return observationRepository.save(existing);

        } catch (Exception e) {
            log.error("Error updating draft: {}", e.getMessage());
            throw new RuntimeException("Failed to update draft: " + e.getMessage());
        }
    }

    @Override
    public Observation processWithAI(String observationId) {
        Observation observation = observationRepository.findById(observationId)
                .orElseThrow(() -> new RuntimeException("Observation not found"));

        observation.setStatus(Observation.ObservationStatus.PROCESSING);
        observationRepository.save(observation);

        try {
            // Call AI service
            AIPredictionResult aiResult = aiIntegrationService.predictCoralHealth(
                    observation.getImageUrl()
            );

            // Set AI result
            observation.setAiResult(aiResult);
            observation.setSeverityScore(aiResult.getSeverityLevel());
            observation.setStatus(Observation.ObservationStatus.COMPLETED);
            observation.setUpdatedAt(LocalDateTime.now());

            log.info("AI processing completed for observation: {}", observationId);

        } catch (Exception e) {
            log.error("AI processing failed: {}", e.getMessage());
            observation.setStatus(Observation.ObservationStatus.PENDING);
        }

        return observationRepository.save(observation);
    }

    @Override
    public Observation updateObservationStatus(String observationId, Observation.ObservationStatus status) {
        Observation observation = observationRepository.findById(observationId)
                .orElseThrow(() -> new RuntimeException("Observation not found"));

        observation.setStatus(status);
        observation.setUpdatedAt(LocalDateTime.now());

        return observationRepository.save(observation);
    }

    @Override
    public void withdrawObservation(String observationId, String userId) {
        Observation observation = observationRepository.findById(observationId)
                .orElseThrow(() -> new RuntimeException("Observation not found"));

        if (!observation.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to withdraw this observation");
        }

        if (observation.getStatus() == Observation.ObservationStatus.COMPLETED) {
            throw new RuntimeException("Completed observations cannot be withdrawn");
        }

        observation.setStatus(Observation.ObservationStatus.WITHDRAWN);
        observation.setUpdatedAt(LocalDateTime.now());

        observationRepository.save(observation);
        log.info("Observation withdrawn: {}", observationId);
    }

    @Override
    public void deleteObservation(String observationId, String userId) {
        // Admin only - implement role check
        Observation observation = observationRepository.findById(observationId)
                .orElseThrow(() -> new RuntimeException("Observation not found"));

        // Delete image from storage
        if (observation.getImageUrl() != null) {
            deleteImageFromStorage(observation.getImageUrl());
        }

        observationRepository.deleteById(observationId);
        log.info("Observation deleted: {} by admin: {}", observationId, userId);
    }

    @Override
    public void deleteAllUserObservations(String userId) {
        List<Observation> observations = observationRepository.findByUserId(userId);

        // Delete all images
        for (Observation obs : observations) {
            if (obs.getImageUrl() != null) {
                deleteImageFromStorage(obs.getImageUrl());
            }
        }

        observationRepository.deleteByUserId(userId);
        log.info("All observations deleted for user: {}", userId);
    }

    @Override
    public Long countUserObservations(String userId) {
        return observationRepository.countByUserId(userId);
    }

    @Override
    public List<Observation> getObservationsByDateRange(LocalDateTime start, LocalDateTime end) {
        return observationRepository.findByCreatedAtBetween(start, end);
    }

    // Private helper methods
    private String saveImageToStorage(MultipartFile image) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String filename = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);

        // Save file
        Files.copy(image.getInputStream(), filePath);

        return filePath.toString();
    }

    private void deleteImageFromStorage(String imageUrl) {
        try {
            Path filePath = Paths.get(imageUrl);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Error deleting image: {}", e.getMessage());
        }
    }

    private void processWithAIAsync(String observationId) {
        // In real implementation, use @Async or message queue
        // For now, call synchronously
        processWithAI(observationId);
    }
}