package com.marineguard.service.member2;

import com.marineguard.model.member2.Observation;
import com.marineguard.model.member2.AIPredictionResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface ObservationService {

    // CREATE operations
    Observation submitObservation(String userId, MultipartFile image, String description,
                                  String location, Double latitude, Double longitude);

    Observation saveDraft(String userId, MultipartFile image, String description,
                          String location, Double latitude, Double longitude);

    // READ operations
    List<Observation> getUserObservations(String userId);

    Optional<Observation> getObservationById(String observationId, String userId);

    List<Observation> getUserObservationsByStatus(String userId, Observation.ObservationStatus status);

    List<Observation> getHighSeverityObservations();

    // UPDATE operations
    Observation updateDraft(String observationId, String userId, MultipartFile image,
                            String description, String location);

    Observation processWithAI(String observationId);

    Observation updateObservationStatus(String observationId, Observation.ObservationStatus status);

    // DELETE operations
    void withdrawObservation(String observationId, String userId);

    void deleteObservation(String observationId, String userId); // Admin only

    void deleteAllUserObservations(String userId); // For account deletion

    // Analytics methods
    Long countUserObservations(String userId);

    List<Observation> getObservationsByDateRange(LocalDateTime start, LocalDateTime end);
}