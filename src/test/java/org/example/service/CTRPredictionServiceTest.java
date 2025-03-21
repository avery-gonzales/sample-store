package org.example.service;

import org.example.model.LinkClick;
import org.example.model.SentTextMessage;
import org.example.repository.LinkClickRepository;
import org.example.repository.SentTextMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CTRPredictionServiceTest {

    @Mock
    private LinkClickRepository linkClickRepository;

    @Mock
    private SentTextMessageRepository sentTextMessageRepository;

    @InjectMocks
    private CTRPredictionService ctrPredictionService;

    private final Long STORE_ID = 1L;
    private List<SentTextMessage> messages;
    private List<LinkClick> clicks;

    @BeforeEach
    void setUp() {
        messages = new ArrayList<>();
        clicks = new ArrayList<>();

        // Create test data for past 30 days
        LocalDateTime now = LocalDateTime.now();

        // Create 4 messages for each of the past 12 days (48 total messages)
        for (int i = 0; i < 12; i++) {
            LocalDateTime day = now.minusDays(i);

            for (int j = 0; j < 4; j++) {
                SentTextMessage message = new SentTextMessage();
                message.setId(i * 4 + j + 1);
                message.setStoreId(STORE_ID.intValue());
                message.setSentAt(day.minusHours(j));
                messages.add(message);
            }

            // Create clicks with variable CTR (between 10% and 75%)
            int clickCount = (int) Math.round(4 * (0.1 + (0.65 * Math.sin(i / 3.0))));
            clickCount = Math.max(0, Math.min(4, clickCount)); // Ensure between 0-4 clicks

            for (int j = 0; j < clickCount; j++) {
                LinkClick click = new LinkClick();
                click.setId(i * 4 + j + 1);
                click.setStoreId(STORE_ID.intValue());
                click.setClickedAt(day.minusHours(j).plusMinutes(30)); // Click 30 mins after message
                clicks.add(click);
            }
        }
    }

    @Test
    void shouldPredictCTRForFutureDays() {
        // Given
        when(sentTextMessageRepository.findByStore_Id(STORE_ID.intValue())).thenReturn(messages);
        when(linkClickRepository.findByStoreId(STORE_ID.intValue())).thenReturn(clicks);

        // When
        Map<String, Double> predictions = ctrPredictionService.predictCTR(STORE_ID, 7);

        // Then
        assertNotNull(predictions);
        assertEquals(7, predictions.size());

        // Verify all predictions are between 0 and 1 (valid CTR range)
        predictions.values().forEach(ctr -> {
            assertTrue(ctr >= 0 && ctr <= 1, "CTR should be between 0 and 1");
        });
    }

    @Test
    void shouldGenerateFallbackPredictionsWhenInsufficientData() {
        // Given
        when(sentTextMessageRepository.findByStore_Id(STORE_ID.intValue())).thenReturn(List.of());
        when(linkClickRepository.findByStoreId(STORE_ID.intValue())).thenReturn(List.of());

        // When
        Map<String, Double> predictions = ctrPredictionService.predictCTR(STORE_ID, 7);

        // Then
        assertNotNull(predictions);
        assertEquals(7, predictions.size());
    }

    @Test
    void shouldUseSimpleRegressionWhenTimeSeriesNotAvailable() {
        // Given
        when(sentTextMessageRepository.findByStore_Id(STORE_ID.intValue())).thenReturn(messages);
        when(linkClickRepository.findByStoreId(STORE_ID.intValue())).thenReturn(clicks);
        
        // Create a spy of the service to simulate the time series forecasting exception
        CTRPredictionService spy = spy(ctrPredictionService);
        doThrow(new RuntimeException("Time series forecasting unavailable"))
                .when(spy).predictUsingTimeSeriesForecasting(anyMap(), anyInt());

        // When
        Map<String, Double> predictions = spy.predictCTR(STORE_ID, 7);

        // Then
        assertNotNull(predictions);
        assertEquals(7, predictions.size());

        // Verify all predictions are between 0 and 1 (valid CTR range)
        predictions.values().forEach(ctr -> {
            assertTrue(ctr >= 0 && ctr <= 1, "CTR should be between 0 and 1");
        });
    }
} 