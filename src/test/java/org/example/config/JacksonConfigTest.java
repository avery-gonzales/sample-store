package org.example.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JacksonConfigTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeLocalDateTime() throws JsonProcessingException {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2023, Month.JANUARY, 1, 12, 0);
        TestClass testObj = new TestClass();
        testObj.setId(1);
        testObj.setDateTime(dateTime);

        // When
        String json = objectMapper.writeValueAsString(testObj);

        // Then
        assertTrue(json.contains("\"dateTime\":\"2023-01-01T12:00:00\""), 
                "LocalDateTime should be serialized in ISO-8601 format");
    }

    @Test
    void shouldDeserializeLocalDateTime() throws JsonProcessingException {
        // Given
        String json = "{\"id\":1,\"dateTime\":\"2023-01-01T12:00:00\"}";

        // When
        TestClass testObj = objectMapper.readValue(json, TestClass.class);

        // Then
        assertEquals(1, testObj.getId(), "ID should match");
        assertEquals(LocalDateTime.of(2023, Month.JANUARY, 1, 12, 0), testObj.getDateTime(),
                "LocalDateTime should be properly deserialized");
    }

    // Test class with LocalDateTime field
    static class TestClass {
        private int id;
        private LocalDateTime dateTime;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        public void setDateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
        }
    }
} 