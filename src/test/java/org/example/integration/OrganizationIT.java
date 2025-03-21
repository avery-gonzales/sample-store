package org.example.integration;

import org.example.model.Organization;
import org.example.repository.OrganizationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OrganizationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrganizationRepository organizationRepository;
    
    @AfterEach
    void cleanup() {
        // Clean up test data after each test
        organizationRepository.deleteAll();
    }

    @Test
    void testGetOrganizations() throws Exception {
        // Setup test data
        Organization org1 = new Organization();
        org1.setName("Integration Test Org 1");
        org1.setCreatedAt(LocalDateTime.now());
        org1.setLastModifiedAt(LocalDateTime.now());
        organizationRepository.save(org1);

        Organization org2 = new Organization();
        org2.setName("Integration Test Org 2");
        org2.setCreatedAt(LocalDateTime.now());
        org2.setLastModifiedAt(LocalDateTime.now());
        organizationRepository.save(org2);

        // Test the endpoint
        mockMvc.perform(get("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.content[*].name", hasItems("Integration Test Org 1", "Integration Test Org 2")));
    }

    @Test
    void testGetOrganizationById() throws Exception {
        // Setup test data
        Organization org = new Organization();
        org.setName("Integration Test Org Detail");
        org.setCreatedAt(LocalDateTime.now());
        org.setLastModifiedAt(LocalDateTime.now());
        org = organizationRepository.save(org);

        // Test the endpoint
        mockMvc.perform(get("/api/organizations/" + org.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(org.getId())))
                .andExpect(jsonPath("$.name", is("Integration Test Org Detail")));
    }

    @Test
    void testGetOrganizationsForDropdown() throws Exception {
        // Setup test data
        Organization org1 = new Organization();
        org1.setName("Dropdown Test Org 1");
        org1.setCreatedAt(LocalDateTime.now());
        org1.setLastModifiedAt(LocalDateTime.now());
        organizationRepository.save(org1);

        Organization org2 = new Organization();
        org2.setName("Dropdown Test Org 2");
        org2.setCreatedAt(LocalDateTime.now());
        org2.setLastModifiedAt(LocalDateTime.now());
        organizationRepository.save(org2);

        // Test the endpoint
        mockMvc.perform(get("/api/organizations/dropdown")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].name", hasItems("Dropdown Test Org 1", "Dropdown Test Org 2")));
    }
} 