package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.CTRPredictionDTO;
import org.example.model.Store;
import org.example.repository.StoreRepository;
import org.example.service.AnalyticsService;
import org.example.service.CTRPredictionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final CTRPredictionService ctrPredictionService;
    private final AnalyticsService analyticsService;
    private final StoreRepository storeRepository;

    /**
     * Endpoint to get CTR predictions for a store for the next N days.
     *
     * @param storeId The ID of the store to predict for
     * @param days    The number of days to predict (defaults to 30 if not specified)
     * @return A CTRPredictionDTO containing the predictions and related data
     */
    @GetMapping("/ctr/{storeId}")
    public ResponseEntity<CTRPredictionDTO> getCTRPrediction(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "30") int days) {

        // Validate input
        if (days < 1 || days > 365) {
            return ResponseEntity.badRequest().build();
        }

        // Get the store to include its name in the response
        Optional<Store> storeOpt = storeRepository.findById(storeId.intValue());
        if (storeOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Store store = storeOpt.get();

        // Get current CTR for comparison
        Map<String, Object> analytics = analyticsService.getStoreAnalytics(storeId);
        double currentCTR = 0.0;
        if (analytics.containsKey("clickThroughRate")) {
            currentCTR = (double) analytics.get("clickThroughRate");
        }

        // Get predictions
        Map<String, Double> predictions = ctrPredictionService.predictCTR(storeId, days);

        // Create and populate the response DTO
        CTRPredictionDTO predictionDTO = new CTRPredictionDTO();
        predictionDTO.setStoreId(storeId);
        predictionDTO.setStoreName(store.getName());
        predictionDTO.setPredictions(predictions);
        predictionDTO.setCurrentCTR(currentCTR);
        predictionDTO.updateCalculatedFields();

        return ResponseEntity.ok(predictionDTO);
    }
} 