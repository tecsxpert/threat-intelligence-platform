package com.internship.tool.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data                       // Lombok: generates getters, setters, equals, hashCode, toString
@Builder                    // Lombok: lets you do ThreatIntelligence.builder().title("x").build()
@NoArgsConstructor          // Lombok: generates empty constructor
@AllArgsConstructor         // Lombok: generates constructor with all fields
@Entity
@Table(name = "threat_intelligence")
@EntityListeners(AuditingEntityListener.class)  // needed for @CreatedDate to work
public class ThreatIntelligence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "Description is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Severity is required")
    @Column(nullable = false, length = 20)
    private String severity;   // LOW, MEDIUM, HIGH, CRITICAL

    @NotNull(message = "Status is required")
    @Column(nullable = false, length = 20)
    private String status;     // OPEN, IN_PROGRESS, RESOLVED, CLOSED

    @Column(length = 500)
    private String source;     // where this threat came from

    @Column(name = "affected_systems", columnDefinition = "TEXT")
    private String affectedSystems;  // which systems are affected

    @Column(name = "ai_description", columnDefinition = "TEXT")
    private String aiDescription;   // filled by the AI service

    @Column(name = "ai_recommendations", columnDefinition = "TEXT")
    private String aiRecommendations;  // filled by the AI service

    // Soft delete — when true, this record is hidden but not removed from DB
    // Deleted records can be recovered if needed
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    // Automatically set when the record is first created
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Automatically updated every time the record is saved
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getAffectedSystems() { return affectedSystems; }
    public void setAffectedSystems(String affectedSystems) { this.affectedSystems = affectedSystems; }
    public String getAiDescription() { return aiDescription; }
    public void setAiDescription(String aiDescription) { this.aiDescription = aiDescription; }
    public String getAiRecommendations() { return aiRecommendations; }
    public void setAiRecommendations(String aiRecommendations) { this.aiRecommendations = aiRecommendations; }
    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}