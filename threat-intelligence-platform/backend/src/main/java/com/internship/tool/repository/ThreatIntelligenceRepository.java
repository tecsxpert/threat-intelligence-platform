package com.internship.tool.repository;

import com.internship.tool.entity.ThreatIntelligence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// @Repository → tells Spring this is a database access class
// JpaRepository<ThreatIntelligence, Long>
//   ThreatIntelligence → which entity/table this manages
//   Long               → the data type of the primary key (id)
//
// By extending JpaRepository you get these FREE methods:
//   save(entity)          → insert or update a row
//   findById(id)          → get one row by id, returns Optional
//   findAll()             → get every row
//   findAll(pageable)     → get one page of rows
//   deleteById(id)        → delete a row
//   count()               → how many rows exist
//   existsById(id)        → does this id exist? returns boolean

@Repository
public interface ThreatIntelligenceRepository
        extends JpaRepository<ThreatIntelligence, Long> {

    // ── HOW SPRING READS METHOD NAMES ───────────────────────────
    //
    // Spring reads the method name and auto-generates the SQL.
    // You never write SQL for these — Spring does it for you.
    //
    // Rules:
    //   findBy       → SELECT ... WHERE
    //   countBy      → SELECT COUNT(*) WHERE
    //   existsBy     → SELECT EXISTS WHERE
    //   And          → AND in the WHERE clause
    //   Or           → OR in the WHERE clause
    //   OrderBy      → ORDER BY
    //   Desc         → DESC
    //   False / True → = false / = true
    //
    // Example: findByDeletedFalseOrderByCreatedAtDesc
    //   translates to:
    //   SELECT * FROM threat_intelligence
    //   WHERE deleted = false
    //   ORDER BY created_at DESC

    // ── CHECK IF TITLE EXISTS ────────────────────────────────────
    // Used by service to prevent duplicate titles
    // SQL: SELECT EXISTS(SELECT 1 FROM threat_intelligence WHERE title = ?)
    boolean existsByTitle(String title);

    // ── FIND ONE BY ID — only if not deleted ────────────────────
    // Safer version of findById() — ignores soft-deleted records
    // SQL: SELECT * FROM threat_intelligence WHERE id = ? AND deleted = false
    Optional<ThreatIntelligence> findByIdAndDeletedFalse(Long id);

    // ── GET ALL — paginated, newest first, not deleted ───────────
    // Pageable carries: page number, page size, sort direction
    // SQL: SELECT * FROM threat_intelligence
    //      WHERE deleted = false
    //      ORDER BY created_at DESC
    //      LIMIT ? OFFSET ?
    Page<ThreatIntelligence> findByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    // ── FILTER BY SEVERITY ───────────────────────────────────────
    // SQL: SELECT * FROM threat_intelligence
    //      WHERE severity = ? AND deleted = false
    //      ORDER BY created_at DESC
    Page<ThreatIntelligence> findBySeverityAndDeletedFalseOrderByCreatedAtDesc(
            String severity, Pageable pageable);

    // ── FILTER BY STATUS ─────────────────────────────────────────
    // SQL: SELECT * FROM threat_intelligence
    //      WHERE status = ? AND deleted = false
    //      ORDER BY created_at DESC
    Page<ThreatIntelligence> findByStatusAndDeletedFalseOrderByCreatedAtDesc(
            String status, Pageable pageable);

    // ── SEARCH BY KEYWORD ────────────────────────────────────────
    // @Query → when Spring cannot figure out the SQL from the method name,
    //          you write it yourself using JPQL (Java Persistence Query Language)
    //
    // JPQL uses class names (ThreatIntelligence) not table names (threat_intelligence)
    // LOWER(...) makes the search case-insensitive
    // CONCAT('%', :keyword, '%') means "contains this word anywhere"
    //
    // :keyword → replaced with the value of the @Param("keyword") argument
    @Query("SELECT t FROM ThreatIntelligence t " +
           "WHERE t.deleted = false " +
           "AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.source) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY t.createdAt DESC")
    List<ThreatIntelligence> searchByKeyword(@Param("keyword") String keyword);

    List<ThreatIntelligence> findBySeverityAndStatusAndDeletedFalse(String severity, String status);

    // ── COUNT METHODS — used by dashboard stats ──────────────────
    // SQL: SELECT COUNT(*) FROM threat_intelligence WHERE deleted = false
    long countByDeletedFalse();

    // SQL: SELECT COUNT(*) FROM threat_intelligence
    //      WHERE severity = ? AND deleted = false
    long countBySeverityAndDeletedFalse(String severity);

    // SQL: SELECT COUNT(*) FROM threat_intelligence
    //      WHERE status = ? AND deleted = false
    long countByStatusAndDeletedFalse(String status);

    // ── GET RECENT RECORDS — for dashboard ──────────────────────
    // Gets the 5 most recently created threats (not deleted)
    // Used by the dashboard "Recent Activity" section
    @Query("SELECT t FROM ThreatIntelligence t " +
           "WHERE t.deleted = false " +
           "ORDER BY t.createdAt DESC")
    List<ThreatIntelligence> findTop5ByDeletedFalseOrderByCreatedAtDesc(
            Pageable pageable);
}