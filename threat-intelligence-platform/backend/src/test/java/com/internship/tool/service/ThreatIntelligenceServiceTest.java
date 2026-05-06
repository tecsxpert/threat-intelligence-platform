package com.internship.tool.service;

import com.internship.tool.entity.ThreatIntelligence;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.exception.ValidationException;
import com.internship.tool.repository.ThreatIntelligenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ThreatIntelligenceServiceTest {

    @Mock
    private ThreatIntelligenceRepository repository;

    @InjectMocks
    private ThreatIntelligenceService service;

    private ThreatIntelligence testThreat;

    @BeforeEach
    void setUp() {
        testThreat = new ThreatIntelligence();
        testThreat.setId(1L);
        testThreat.setTitle("Test Threat");
        testThreat.setDescription("Test Description");
        testThreat.setSeverity("HIGH");
        testThreat.setStatus("OPEN");
    }

    @Test
    void create_ValidThreat_ReturnsSavedThreat() {
        when(repository.existsByTitle("Test Threat")).thenReturn(false);
        when(repository.save(any(ThreatIntelligence.class))).thenReturn(testThreat);

        ThreatIntelligence result = service.create(testThreat);

        assertNotNull(result);
        assertEquals("Test Threat", result.getTitle());
        verify(repository, times(1)).save(testThreat);
    }

    @Test
    void create_DuplicateTitle_ThrowsValidationException() {
        when(repository.existsByTitle("Test Threat")).thenReturn(true);

        assertThrows(ValidationException.class, () -> service.create(testThreat));
        verify(repository, never()).save(any());
    }

    @Test
    void findById_ExistingId_ReturnsThreat() {
        when(repository.findById(1L)).thenReturn(Optional.of(testThreat));

        ThreatIntelligence result = service.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void findById_NonExistingId_ThrowsResourceNotFoundException() {
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(2L));
    }

    @Test
    void update_ExistingThreat_UpdatesAndReturns() {
        when(repository.findById(1L)).thenReturn(Optional.of(testThreat));
        when(repository.save(any(ThreatIntelligence.class))).thenReturn(testThreat);

        testThreat.setTitle("Updated Title");
        ThreatIntelligence result = service.update(1L, testThreat);

        assertEquals("Updated Title", result.getTitle());
        verify(repository, times(1)).save(testThreat);
    }

    @Test
    void update_DuplicateTitle_ThrowsValidationException() {
        ThreatIntelligence updateData = new ThreatIntelligence();
        updateData.setTitle("Other Title");
        updateData.setDescription("Desc");
        updateData.setSeverity("HIGH");
        updateData.setStatus("OPEN");

        when(repository.findById(1L)).thenReturn(Optional.of(testThreat));
        when(repository.existsByTitle("Other Title")).thenReturn(true);

        assertThrows(ValidationException.class, () -> service.update(1L, updateData));
    }

    @Test
    void softDelete_ExistingThreat_MarksAsDeleted() {
        when(repository.findById(1L)).thenReturn(Optional.of(testThreat));

        service.softDelete(1L);

        assertTrue(testThreat.getDeleted());
        verify(repository, times(1)).save(testThreat);
    }

    @Test
    void search_ValidKeyword_ReturnsList() {
        when(repository.searchByKeyword("Malware")).thenReturn(java.util.List.of(testThreat));

        java.util.List<ThreatIntelligence> results = service.search("Malware");

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }

    @Test
    void search_EmptyKeyword_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> service.search("   "));
    }

    @Test
    void getStats_ReturnsCorrectStats() {
        when(repository.countByDeletedFalse()).thenReturn(10L);
        when(repository.countBySeverityAndDeletedFalse("CRITICAL")).thenReturn(2L);
        when(repository.countBySeverityAndDeletedFalse("HIGH")).thenReturn(5L);
        when(repository.countByStatusAndDeletedFalse("OPEN")).thenReturn(8L);

        ThreatIntelligenceService.ThreatStatsDTO stats = service.getStats();

        assertEquals(10L, stats.total());
        assertEquals(2L, stats.critical());
        assertEquals(5L, stats.high());
        assertEquals(8L, stats.open());
    }
}

