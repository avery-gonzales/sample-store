package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/store/{storeId}")
    public ResponseEntity<Map<String, Object>> getStoreAnalytics(@PathVariable Integer storeId) {
        return ResponseEntity.ok(analyticsService.getStoreAnalytics(storeId));
    }

    @GetMapping("/store/{storeId}/recent")
    public ResponseEntity<Map<String, Object>> getRecentAnalytics(
            @PathVariable Integer storeId,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(analyticsService.getRecentAnalytics(storeId, days));
    }
} 