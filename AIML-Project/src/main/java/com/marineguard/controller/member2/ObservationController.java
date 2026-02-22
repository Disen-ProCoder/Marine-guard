package com.marineguard.controller.member2;

import com.marineguard.model.member2.Observation;
import com.marineguard.service.member2.ObservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/observations")
@RequiredArgsConstructor
public class ObservationController {

    private final ObservationService observationService;

    /**
     * CREATE: Submit new observation
     * Endpoint: POST /api/observations/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<?> submitObservation(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude) {

        try {
            String userId = getCurrentUserId();

            Observation observation = observationService.submitObservation(
                    userId, image, description, location, latitude, longitude
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Observation submitted successfully");
            response.put("observationId", observation.getId());
            response.put("status", observation.getStatus());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Upload failed: {}", e.getMessage());

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Upload failed: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * CREATE: Save as draft
     * Endpoint: POST /api/observations/draft
     */
    @PostMapping("/draft")
    public ResponseEntity<?> saveDraft(
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude) {

        try {
            String userId = getCurrentUserId();

            Observation draft = observationService.saveDraft(
                    userId, image, description, location, latitude, longitude
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Draft saved successfully");
            response.put("draftId", draft.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to save draft: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * READ: Get all observations for current user
     * Endpoint: GET /api/observations/my-observations
     */
    @GetMapping("/my-observations")
    public ResponseEntity<?> getUserObservations() {
        try {
            String userId = getCurrentUserId();
            List<Observation> observations = observationService.getUserObservations(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", observations.size());
            response.put("observations", observations);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to fetch observations: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * READ: Get observation by ID
     * Endpoint: GET /api/observations/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getObservationById(@PathVariable String id) {
        try {
            String userId = getCurrentUserId();
            Optional<Observation> observation = observationService.getObservationById(id, userId);

            if (observation.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("observation", observation.get());

                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Observation not found or access denied");

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to fetch observation: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * READ: Get observations by status
     * Endpoint: GET /api/observations/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getObservationsByStatus(@PathVariable String status) {
        try {
            String userId = getCurrentUserId();

            Observation.ObservationStatus observationStatus;
            try {
                observationStatus = Observation.ObservationStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Invalid status. Valid values: PENDING, PROCESSING, COMPLETED, DRAFT, WITHDRAWN, REJECTED");

                return ResponseEntity.badRequest().body(error);
            }

            List<Observation> observations = observationService.getUserObservationsByStatus(userId, observationStatus);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("status", status);
            response.put("count", observations.size());
            response.put("observations", observations);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to fetch observations: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * READ: Get high severity observations
     * Endpoint: GET /api/observations/high-severity
     */
    @GetMapping("/high-severity")
    public ResponseEntity<?> getHighSeverityObservations() {
        try {
            List<Observation> observations = observationService.getHighSeverityObservations();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", observations.size());
            response.put("observations", observations);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to fetch high severity observations: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * UPDATE: Edit draft
     * Endpoint: PUT /api/observations/draft/{id}
     */
    @PutMapping("/draft/{id}")
    public ResponseEntity<?> updateDraft(
            @PathVariable String id,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "location", required = false) String location) {

        try {
            String userId = getCurrentUserId();

            Observation updated = observationService.updateDraft(
                    id, userId, image, description, location
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Draft updated successfully");
            response.put("observation", updated);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to update draft: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * UPDATE: Process observation with AI (trigger)
     * Endpoint: POST /api/observations/{id}/process
     */
    @PostMapping("/{id}/process")
    public ResponseEntity<?> processWithAI(@PathVariable String id) {
        try {
            String userId = getCurrentUserId();

            // Verify ownership first
            Optional<Observation> existing = observationService.getObservationById(id, userId);
            if (existing.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Observation not found or access denied");

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Observation processed = observationService.processWithAI(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "AI processing completed");
            response.put("observation", processed);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "AI processing failed: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * DELETE: Withdraw pending observation
     * Endpoint: DELETE /api/observations/pending/{id}
     */
    @DeleteMapping("/pending/{id}")
    public ResponseEntity<?> withdrawObservation(@PathVariable String id) {
        try {
            String userId = getCurrentUserId();

            observationService.withdrawObservation(id, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Observation withdrawn successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to withdraw observation: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * DELETE: Delete observation (Admin only)
     * Endpoint: DELETE /api/observations/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteObservation(@PathVariable String id) {
        try {
            String userId = getCurrentUserId();
            // TODO: Add role check for admin

            observationService.deleteObservation(id, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Observation deleted successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to delete observation: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Helper method to get current authenticated user ID
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); // Returns username/userId
    }
}