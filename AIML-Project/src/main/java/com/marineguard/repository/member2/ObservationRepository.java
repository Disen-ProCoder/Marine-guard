package com.marineguard.repository.member2;

import com.marineguard.model.member2.Observation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ObservationRepository extends MongoRepository<Observation, String> {

    // Find by userId
    List<Observation> findByUserId(String userId);

    // Find by status
    List<Observation> findByStatus(Observation.ObservationStatus status);

    // Find by userId and status
    List<Observation> findByUserIdAndStatus(String userId, Observation.ObservationStatus status);

    // Find observations with high severity
    @Query("{'aiResult.severityLevel': {$gte: 3}}")
    List<Observation> findHighSeverityObservations();

    // Find observations by location (simple text search)
    List<Observation> findByLocationContaining(String location);

    // Find observations within date range
    List<Observation> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Find observations by userId and date range
    List<Observation> findByUserIdAndCreatedAtBetween(String userId, LocalDateTime start, LocalDateTime end);

    // Count observations by status
    Long countByStatus(Observation.ObservationStatus status);

    // Count observations by userId
    Long countByUserId(String userId);

    // Delete by userId (for account deletion)
    void deleteByUserId(String userId);
}