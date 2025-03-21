package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CTRPredictionDTO {
    private Long storeId;
    private String storeName;
    private Map<String, Double> predictions;
    private double currentCTR;
    private double averagePredictedCTR;
    private double predictedCTRChange;
    
    /**
     * Calculates the average predicted CTR from the predictions map.
     * @return The average predicted CTR value
     */
    public double calculateAveragePredictedCTR() {
        if (predictions == null || predictions.isEmpty()) {
            return 0.0;
        }
        
        return predictions.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }
    
    /**
     * Calculates the predicted change in CTR compared to the current CTR.
     * @return The percentage change in CTR (positive or negative)
     */
    public double calculatePredictedCTRChange() {
        if (currentCTR == 0) {
            return 0.0;
        }
        
        double avgPredicted = calculateAveragePredictedCTR();
        return (avgPredicted - currentCTR) / currentCTR;
    }
    
    /**
     * Updates the calculated fields based on the current data.
     */
    public void updateCalculatedFields() {
        this.averagePredictedCTR = calculateAveragePredictedCTR();
        this.predictedCTRChange = calculatePredictedCTRChange();
    }
} 