package org.example.repository;

import org.example.model.Organization;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class OrganizationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Test
    void testFindById() {
        // Given
        Organization org = new Organization();
        org.setName("Test Organization");
        org.setCreatedAt(LocalDateTime.now());
        org.setLastModifiedAt(LocalDateTime.now());
        
        entityManager.persist(org);
        entityManager.flush();

        // When
        Optional<Organization> found = organizationRepository.findById(org.getId());

        // Then
        assertTrue(found.isPresent(), "Organization should be found");
        assertEquals(org.getName(), found.get().getName(), "Names should match");
    }

    @Test
    void testFindAll() {
        // Given
        Organization org1 = new Organization();
        org1.setName("Test Organization 1");
        org1.setCreatedAt(LocalDateTime.now());
        org1.setLastModifiedAt(LocalDateTime.now());

        Organization org2 = new Organization();
        org2.setName("Test Organization 2");
        org2.setCreatedAt(LocalDateTime.now());
        org2.setLastModifiedAt(LocalDateTime.now());

        entityManager.persist(org1);
        entityManager.persist(org2);
        entityManager.flush();

        // When
        List<Organization> organizations = organizationRepository.findAll();

        // Then
        assertFalse(organizations.isEmpty(), "List should not be empty");
        assertTrue(organizations.size() >= 2, "Should have at least 2 organizations");
        assertTrue(organizations.stream().anyMatch(o -> o.getName().equals("Test Organization 1")), 
                "Should contain Test Organization 1");
        assertTrue(organizations.stream().anyMatch(o -> o.getName().equals("Test Organization 2")), 
                "Should contain Test Organization 2");
    }

    @Test
    void testSave() {
        // Given
        Organization org = new Organization();
        org.setName("New Organization");
        org.setCreatedAt(LocalDateTime.now());
        org.setLastModifiedAt(LocalDateTime.now());

        // When
        Organization saved = organizationRepository.save(org);
        
        // Then
        assertNotNull(saved.getId(), "ID should be generated");
        assertEquals("New Organization", saved.getName(), "Name should match");
        
        // Verify it was saved to the database
        Organization found = entityManager.find(Organization.class, saved.getId());
        assertNotNull(found, "Organization should be found in database");
        assertEquals("New Organization", found.getName(), "Name should match");
    }

    @Test
    void testDelete() {
        // Given
        Organization org = new Organization();
        org.setName("Organization to Delete");
        org.setCreatedAt(LocalDateTime.now());
        org.setLastModifiedAt(LocalDateTime.now());
        
        org = entityManager.persist(org);
        entityManager.flush();
        
        // When
        organizationRepository.delete(org);
        
        // Then
        Organization found = entityManager.find(Organization.class, org.getId());
        assertNull(found, "Organization should be deleted");
    }
} 