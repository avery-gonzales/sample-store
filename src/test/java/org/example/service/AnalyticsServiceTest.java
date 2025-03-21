package org.example.service;

import org.example.repository.LinkClickRepository;
import org.example.repository.SentTextMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private SentTextMessageRepository sentTextMessageRepository;

    @Mock
    private LinkClickRepository linkClickRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private final Integer STORE_ID = 1;

    @BeforeEach
    void setUp() {
        // Common setup
    }

    @Test
    void testGetStoreAnalytics() {
        // Given
        Long totalMessages = 100L;
        Long totalClicks = 25L;
        double expectedCtr = 25.0; // 25/100 * 100 = 25%

        // Monthly data
        Object[] janMessages = new Object[]{1, 2023, 40L};
        Object[] febMessages = new Object[]{2, 2023, 60L};
        List<Object[]> messagesByMonth = Arrays.asList(janMessages, febMessages);

        Object[] janClicks = new Object[]{1, 2023, 10L};
        Object[] febClicks = new Object[]{2, 2023, 15L};
        List<Object[]> clicksByMonth = Arrays.asList(janClicks, febClicks);

        // Template data
        Object[] template1Messages = new Object[]{101, 70L};
        Object[] template2Messages = new Object[]{102, 30L};
        List<Object[]> messagesByTemplate = Arrays.asList(template1Messages, template2Messages);

        Object[] template1Clicks = new Object[]{101, 15L};
        Object[] template2Clicks = new Object[]{102, 10L};
        List<Object[]> clicksByTemplate = Arrays.asList(template1Clicks, template2Clicks);

        // Mock repository responses
        when(sentTextMessageRepository.countMessagesByStore(STORE_ID)).thenReturn(totalMessages);
        when(linkClickRepository.countClicksByStore(STORE_ID)).thenReturn(totalClicks);
        when(sentTextMessageRepository.countMessagesByMonth(STORE_ID)).thenReturn(messagesByMonth);
        when(linkClickRepository.countClicksByMonth(STORE_ID)).thenReturn(clicksByMonth);
        when(sentTextMessageRepository.countMessagesByTemplate(STORE_ID)).thenReturn(messagesByTemplate);
        when(linkClickRepository.countClicksByTemplate(STORE_ID)).thenReturn(clicksByTemplate);

        // When
        Map<String, Object> analytics = analyticsService.getStoreAnalytics(STORE_ID);

        // Then
        assertEquals(totalMessages, analytics.get("totalMessages"), "Total messages should match");
        assertEquals(totalClicks, analytics.get("totalClicks"), "Total clicks should match");
        assertEquals(expectedCtr, analytics.get("clickThroughRate"), "CTR should match");

        // Verify messagesByMonth
        List<Map<String, Object>> resultMessagesByMonth = (List<Map<String, Object>>) analytics.get("messagesByMonth");
        assertEquals(2, resultMessagesByMonth.size(), "Should have 2 months of message data");
        assertEquals(1, resultMessagesByMonth.get(0).get("month"), "First month should be January");
        assertEquals(40L, resultMessagesByMonth.get(0).get("count"), "January message count should match");

        // Verify clicksByMonth
        List<Map<String, Object>> resultClicksByMonth = (List<Map<String, Object>>) analytics.get("clicksByMonth");
        assertEquals(2, resultClicksByMonth.size(), "Should have 2 months of click data");
        assertEquals(1, resultClicksByMonth.get(0).get("month"), "First month should be January");
        assertEquals(10L, resultClicksByMonth.get(0).get("count"), "January click count should match");

        // Verify CTR by template
        Map<Integer, Double> ctrByTemplate = (Map<Integer, Double>) analytics.get("ctrByTemplate");
        assertEquals(2, ctrByTemplate.size(), "Should have CTR for 2 templates");
        assertEquals(21.43, ctrByTemplate.get(101), 0.01, "Template 101 CTR should be 21.43%");
        assertEquals(33.33, ctrByTemplate.get(102), 0.01, "Template 102 CTR should be 33.33%");
    }

    @Test
    void testGetRecentAnalytics() {
        // Given
        int days = 7;
        Long recentMessages = 42L;
        
        // Mock repository responses
        when(sentTextMessageRepository.countMessagesByStoreAndDateRange(
                eq(STORE_ID), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(recentMessages);

        // When
        Map<String, Object> analytics = analyticsService.getRecentAnalytics(STORE_ID, days);

        // Then
        assertEquals("7 days", analytics.get("period"), "Period should match");
        assertEquals(recentMessages, analytics.get("recentMessages"), "Recent messages count should match");
    }

    @Test
    void testGetStoreAnalyticsWithNoData() {
        // Given
        when(sentTextMessageRepository.countMessagesByStore(STORE_ID)).thenReturn(0L);
        when(linkClickRepository.countClicksByStore(STORE_ID)).thenReturn(0L);
        when(sentTextMessageRepository.countMessagesByMonth(STORE_ID)).thenReturn(List.of());
        when(linkClickRepository.countClicksByMonth(STORE_ID)).thenReturn(List.of());
        when(sentTextMessageRepository.countMessagesByTemplate(STORE_ID)).thenReturn(List.of());
        when(linkClickRepository.countClicksByTemplate(STORE_ID)).thenReturn(List.of());

        // When
        Map<String, Object> analytics = analyticsService.getStoreAnalytics(STORE_ID);

        // Then
        assertEquals(0L, analytics.get("totalMessages"), "Total messages should be 0");
        assertEquals(0L, analytics.get("totalClicks"), "Total clicks should be 0");
        assertEquals(0.0, analytics.get("clickThroughRate"), "CTR should be 0");
        assertTrue(((List<?>) analytics.get("messagesByMonth")).isEmpty(), "Message by month should be empty");
        assertTrue(((List<?>) analytics.get("clicksByMonth")).isEmpty(), "Clicks by month should be empty");
        assertTrue(((Map<?, ?>) analytics.get("messagesByTemplate")).isEmpty(), "Messages by template should be empty");
        assertTrue(((Map<?, ?>) analytics.get("clicksByTemplate")).isEmpty(), "Clicks by template should be empty");
        assertTrue(((Map<?, ?>) analytics.get("ctrByTemplate")).isEmpty(), "CTR by template should be empty");
    }
} 