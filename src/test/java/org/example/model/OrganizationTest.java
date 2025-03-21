package org.example.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class OrganizationTest {

    @Test
    void testEqualsAndHashCode() {
        // Given
        Organization org1 = new Organization();
        org1.setId(1);
        org1.setName("Test Organization");

        Organization org2 = new Organization();
        org2.setId(1);
        org2.setName("Different Name"); // Name should not affect equality

        Organization org3 = new Organization();
        org3.setId(2);
        org3.setName("Test Organization");

        // Then
        assertEquals(org1, org2, "Organizations with same ID should be equal");
        assertNotEquals(org1, org3, "Organizations with different IDs should not be equal");
        assertEquals(org1.hashCode(), org2.hashCode(), "Hash codes should be equal for equal organizations");
        assertNotEquals(org1.hashCode(), org3.hashCode(), "Hash codes should differ for different organizations");
    }

    @Test
    void testGettersAndSetters() {
        // Given
        Organization org = new Organization();
        Integer id = 1;
        String name = "Test Organization";
        LocalDateTime now = LocalDateTime.now();
        HashSet<Store> stores = new HashSet<>();
        Store store = new Store();
        store.setId(1);
        stores.add(store);

        // When
        org.setId(id);
        org.setName(name);
        org.setCreatedAt(now);
        org.setLastModifiedAt(now);
        org.setStores(stores);

        // Then
        assertEquals(id, org.getId(), "ID should match");
        assertEquals(name, org.getName(), "Name should match");
        assertEquals(now, org.getCreatedAt(), "Created at should match");
        assertEquals(now, org.getLastModifiedAt(), "Last modified at should match");
        assertEquals(stores, org.getStores(), "Stores should match");
        assertEquals(1, org.getStores().size(), "Should have one store");
    }

    @Test
    void testToString() {
        // Given
        Organization org = new Organization();
        org.setId(1);
        org.setName("Test Organization");

        // When
        String toString = org.toString();

        // Then
        assertTrue(toString.contains("id=1"), "toString should contain ID");
        assertTrue(toString.contains("name=Test Organization"), "toString should contain name");
    }
} 