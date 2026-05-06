package com.internship.tool.service;

import com.internship.tool.entity.ThreatIntelligence;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.exception.ValidationException;
import com.internship.tool.repository.ThreatIntelligenceRepository;
import org.springframework.lang.NonNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// @Service tells Spring "this is a service class, manage it for me"
// @RequiredArgsConstructor (Lombok) auto-generates a constructor for all final fields
// @Slf4j (Lombok) gives you a log object to write log messages
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ThreatIntelligenceService {

    private static final Logger log = LoggerFactory.getLogger(ThreatIntelligenceService.class);

    private final ThreatIntelligenceRepository repository;

    public ThreatIntelligenceService(ThreatIntelligenceRepository repository) {
        this.repository = repository;
    }

    // ─────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────

    // @Transactional means: if anything fails inside this method,
    // the database automatically rolls back — no partial saves
    @Transactional
    public ThreatIntelligence create(ThreatIntelligence threat) {
        log.info("Creating new threat intelligence record: {}", threat.getTitle());

        // Validate before saving
        validateThreat(threat);

        // Check for duplicate title
        if (repository.existsByTitle(threat.getTitle())) {
            throw new ValidationException("A threat record with this title already exists: " + threat.getTitle());
        }

        ThreatIntelligence saved = repository.save(threat);
        log.info("Successfully created threat with id: {}", saved.getId());
        return saved;
    }

    // ─────────────────────────────────────────────
    // READ — Get all (paginated)
    // ─────────────────────────────────────────────

    // @Cacheable means: first call hits the database,
    // result is stored in Redis, next call returns from Redis instantly
    @Cacheable(value = "threats", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<ThreatIntelligence> findAll(Pageable pageable) {
        log.info("Fetching all threat records, page: {}", pageable.getPageNumber());
        return repository.findAll(pageable);
    }

    // ─────────────────────────────────────────────
    // READ — Get one by ID
    // ─────────────────────────────────────────────

    @Cacheable(value = "threat", key = "#id")
    @Transactional(readOnly = true)
    public ThreatIntelligence findById(@NonNull Long id) {
        log.info("Fetching threat with id: {}", id);

        // If not found, throw our custom exception — controller will return 404
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ThreatIntelligence", id));
    }

    // ─────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────

    // @CacheEvict clears the cache when data changes
    // so the next GET returns fresh data, not stale cached data
    @CacheEvict(value = {"threats", "threat"}, allEntries = true)
    @Transactional
    public ThreatIntelligence update(@NonNull Long id, ThreatIntelligence updatedThreat) {
        log.info("Updating threat with id: {}", id);

        // First check the record exists
        ThreatIntelligence existing = findById(id);

        // Validate new data
        validateThreat(updatedThreat);

        // Check if new title conflicts with another record
        if (!existing.getTitle().equals(updatedThreat.getTitle())
                && repository.existsByTitle(updatedThreat.getTitle())) {
            throw new ValidationException("Another threat record already has the title: " + updatedThreat.getTitle());
        }

        // Copy new values onto existing record
        existing.setTitle(updatedThreat.getTitle());
        existing.setDescription(updatedThreat.getDescription());
        existing.setSeverity(updatedThreat.getSeverity());
        existing.setStatus(updatedThreat.getStatus());
        existing.setSource(updatedThreat.getSource());
        existing.setAffectedSystems(updatedThreat.getAffectedSystems());

        ThreatIntelligence updated = repository.save(existing);
        log.info("Successfully updated threat with id: {}", updated.getId());
        return updated;
    }

    // ─────────────────────────────────────────────
    // DELETE (soft delete — marks as deleted, does not remove from DB)
    // ─────────────────────────────────────────────

    @CacheEvict(value = {"threats", "threat"}, allEntries = true)
    @Transactional
    public void softDelete(@NonNull Long id) {
        log.info("Soft deleting threat with id: {}", id);

        ThreatIntelligence existing = findById(id);
        existing.setDeleted(true);
        repository.save(existing);

        log.info("Successfully soft deleted threat with id: {}", id);
    }

    // ─────────────────────────────────────────────
    // SEARCH
    // ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ThreatIntelligence> search(String keyword) {
        log.info("Searching threats with keyword: {}", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ValidationException("Search keyword cannot be empty");
        }

        return repository.searchByKeyword(keyword.trim());
    }

    // ─────────────────────────────────────────────
    // STATS — used by the dashboard
    // ─────────────────────────────────────────────

    @Cacheable(value = "threat-stats")
    @Transactional(readOnly = true)
    public ThreatStatsDTO getStats() {
        log.info("Fetching threat statistics");

        long total = repository.countByDeletedFalse();
        long critical = repository.countBySeverityAndDeletedFalse("CRITICAL");
        long high = repository.countBySeverityAndDeletedFalse("HIGH");
        long open = repository.countByStatusAndDeletedFalse("OPEN");

        return new ThreatStatsDTO(total, critical, high, open);
    }

    // ─────────────────────────────────────────────
    // PRIVATE HELPER — input validation
    // ─────────────────────────────────────────────

    // This runs before every create and update
    // If anything is invalid, it throws ValidationException immediately
    // and the save never happens
    private void validateThreat(ThreatIntelligence threat) {

        if (threat.getTitle() == null || threat.getTitle().trim().isEmpty()) {
            throw new ValidationException("Title is required");
        }

        if (threat.getTitle().length() > 200) {
            throw new ValidationException("Title cannot exceed 200 characters");
        }

        if (threat.getDescription() == null || threat.getDescription().trim().isEmpty()) {
            throw new ValidationException("Description is required");
        }

        if (threat.getSeverity() == null) {
            throw new ValidationException("Severity is required");
        }

        // Severity must be one of the allowed values
        List<String> allowedSeverities = List.of("LOW", "MEDIUM", "HIGH", "CRITICAL");
        if (!allowedSeverities.contains(threat.getSeverity().toUpperCase())) {
            throw new ValidationException(
                "Invalid severity. Allowed values: LOW, MEDIUM, HIGH, CRITICAL"
            );
        }

        if (threat.getStatus() == null) {
            throw new ValidationException("Status is required");
        }

        List<String> allowedStatuses = List.of("OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED");
        if (!allowedStatuses.contains(threat.getStatus().toUpperCase())) {
            throw new ValidationException(
                "Invalid status. Allowed values: OPEN, IN_PROGRESS, RESOLVED, CLOSED"
            );
        }
    }

    // ─────────────────────────────────────────────
    // INNER DTO CLASS — carries stats data
    // ─────────────────────────────────────────────

    // DTO = Data Transfer Object — just a simple container to carry data
    // This one carries the 4 numbers shown on the dashboard
    public record ThreatStatsDTO(
        long total,
        long critical,
        long high,
        long open
    ) {}
}