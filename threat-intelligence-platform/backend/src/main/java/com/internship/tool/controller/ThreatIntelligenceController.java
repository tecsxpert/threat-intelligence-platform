package com.internship.tool.controller;

import com.internship.tool.entity.ThreatIntelligence;
import com.internship.tool.service.ThreatIntelligenceService;
import com.internship.tool.service.ThreatIntelligenceService.ThreatStatsDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/threats")
@CrossOrigin(origins = "*") // Allows the frontend to communicate with this API
public class ThreatIntelligenceController {

    private final ThreatIntelligenceService threatService;

    public ThreatIntelligenceController(ThreatIntelligenceService threatService) {
        this.threatService = threatService;
    }

    // 1. CREATE
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ThreatIntelligence> createThreat(@Valid @RequestBody ThreatIntelligence threat) {
        ThreatIntelligence created = threatService.create(threat);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // 2. READ ALL (Paginated)
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<ThreatIntelligence>> getAllThreats(Pageable pageable) {
        return ResponseEntity.ok(threatService.findAll(pageable));
    }

    // 3. READ ONE
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ThreatIntelligence> getThreatById(@PathVariable @NonNull Long id) {
        return ResponseEntity.ok(threatService.findById(id));
    }

    // 4. UPDATE
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ThreatIntelligence> updateThreat(
            @PathVariable @NonNull Long id, 
            @Valid @RequestBody ThreatIntelligence updatedThreat) {
        ThreatIntelligence updated = threatService.update(id, updatedThreat);
        return ResponseEntity.ok(updated);
    }

    // 5. DELETE (Soft Delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteThreat(@PathVariable @NonNull Long id) {
        threatService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    // 6. SEARCH
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<ThreatIntelligence>> searchThreats(@RequestParam String keyword) {
        return ResponseEntity.ok(threatService.search(keyword));
    }

    // 7. STATS (Dashboard data)
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ThreatStatsDTO> getStats() {
        return ResponseEntity.ok(threatService.getStats());
    }
}
