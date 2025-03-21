package org.example.service;

import org.example.dto.OrganizationSummaryDTO;
import org.example.model.Organization;
import org.example.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private OrganizationService organizationService;

    private Organization organization1;
    private Organization organization2;

    @BeforeEach
    void setUp() {
        // Prepare test data
        organization1 = new Organization();
        organization1.setId(1);
        organization1.setName("Test Organization 1");
        organization1.setCreatedAt(LocalDateTime.now());
        organization1.setLastModifiedAt(LocalDateTime.now());

        organization2 = new Organization();
        organization2.setId(2);
        organization2.setName("Test Organization 2");
        organization2.setCreatedAt(LocalDateTime.now());
        organization2.setLastModifiedAt(LocalDateTime.now());
    }

    @Test
    void testGetAllOrganizations() {
        // Given
        List<Organization> organizationList = Arrays.asList(organization1, organization2);
        when(organizationRepository.findAll()).thenReturn(organizationList);

        // When
        List<Organization> result = organizationService.getAllOrganizations();

        // Then
        assertEquals(2, result.size(), "Should return 2 organizations");
        assertEquals("Test Organization 1", result.get(0).getName(), "First organization name should match");
        assertEquals("Test Organization 2", result.get(1).getName(), "Second organization name should match");
        verify(organizationRepository, times(1)).findAll();
    }

    @Test
    void testGetAllOrganizationsPaged() {
        // Given
        List<Organization> organizationList = Arrays.asList(organization1, organization2);
        Page<Organization> organizationPage = new PageImpl<>(organizationList);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(organizationRepository.findAll(pageable)).thenReturn(organizationPage);

        // When
        Page<OrganizationSummaryDTO> result = organizationService.getAllOrganizationsPaged(pageable);

        // Then
        assertEquals(2, result.getContent().size(), "Should return 2 organizations");
        assertEquals("Test Organization 1", result.getContent().get(0).getName(), "First organization name should match");
        assertEquals("Test Organization 2", result.getContent().get(1).getName(), "Second organization name should match");
        verify(organizationRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetOrganizationById() {
        // Given
        when(organizationRepository.findById(1)).thenReturn(Optional.of(organization1));

        // When
        Organization result = organizationService.getOrganizationById(1);

        // Then
        assertNotNull(result, "Should return an organization");
        assertEquals("Test Organization 1", result.getName(), "Organization name should match");
        verify(organizationRepository, times(1)).findById(1);
    }

    @Test
    void testGetOrganizationByIdNotFound() {
        // Given
        when(organizationRepository.findById(99)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            organizationService.getOrganizationById(99);
        }, "Should throw RuntimeException when organization not found");
        
        assertEquals("Organization not found with id: 99", exception.getMessage(), 
                "Exception message should match");
        verify(organizationRepository, times(1)).findById(99);
    }
} 