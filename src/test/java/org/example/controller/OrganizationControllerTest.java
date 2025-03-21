package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.OrganizationDTO;
import org.example.dto.OrganizationSummaryDTO;
import org.example.model.Organization;
import org.example.service.OrganizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrganizationController.class)
class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrganizationService organizationService;

    private Organization organization1;
    private Organization organization2;
    private OrganizationSummaryDTO summaryDTO1;
    private OrganizationSummaryDTO summaryDTO2;

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

        summaryDTO1 = new OrganizationSummaryDTO();
        summaryDTO1.setId(1);
        summaryDTO1.setName("Test Organization 1");

        summaryDTO2 = new OrganizationSummaryDTO();
        summaryDTO2.setId(2);
        summaryDTO2.setName("Test Organization 2");
    }

    @Test
    void testGetAllOrganizations() throws Exception {
        // Given
        List<OrganizationSummaryDTO> dtoList = Arrays.asList(summaryDTO1, summaryDTO2);
        Page<OrganizationSummaryDTO> page = new PageImpl<>(dtoList);
        
        when(organizationService.getAllOrganizationsPaged(any(PageRequest.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/organizations")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].name", is("Test Organization 1")))
                .andExpect(jsonPath("$.content[1].id", is(2)))
                .andExpect(jsonPath("$.content[1].name", is("Test Organization 2")));
    }

    @Test
    void testGetOrganizationsList() throws Exception {
        // Given
        List<Organization> organizations = Arrays.asList(organization1, organization2);
        when(organizationService.getAllOrganizations()).thenReturn(organizations);

        // When & Then
        mockMvc.perform(get("/api/organizations/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Test Organization 1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Test Organization 2")));
    }

    @Test
    void testGetOrganizationById() throws Exception {
        // Given
        when(organizationService.getOrganizationById(1)).thenReturn(organization1);

        // When & Then
        mockMvc.perform(get("/api/organizations/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Organization 1")));
    }

    @Test
    void testGetOrganizationsForDropdown() throws Exception {
        // Given
        List<Organization> organizations = Arrays.asList(organization1, organization2);
        when(organizationService.getAllOrganizations()).thenReturn(organizations);

        // When & Then
        mockMvc.perform(get("/api/organizations/dropdown")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Test Organization 1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Test Organization 2")));
    }
} 