package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.repository.LinkClickRepository;
import org.example.repository.SentTextMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import weka.classifiers.evaluation.NumericPrediction;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.timeseries.WekaForecaster;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CTRPredictionService {
    private static final Logger logger = LoggerFactory.getLogger(CTRPredictionService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final LinkClickRepository linkClickRepository;
    private final SentTextMessageRepository sentTextMessageRepository;

    /**
     * Predicts the click-through rate (CTR) for a given store for the specified number of days into the future.
     *
     * @param storeId The ID of the store to predict for
     * @param days    The number of days to predict into the future
     * @return A map of dates to predicted CTR values
     */
    public Map<String, Double> predictCTR(Long storeId, int days) {
        try {
            // Get historical CTR data
            Map<LocalDate, Double> historicalCTR = getHistoricalCTR(storeId);

            if (historicalCTR.size() < 7) {
                // Need at least 7 days of data for a reasonable forecast
                logger.warn("Not enough historical data for store {}: {} days available", storeId, historicalCTR.size());
                return generateFallbackPredictions(historicalCTR, days);
            }

            try {
                // Try to use the advanced time series forecasting if available
                return predictUsingTimeSeriesForecasting(historicalCTR, days);
            } catch (Exception e) {
                logger.warn("Error using time series forecasting, falling back to simple regression: {}", e.getMessage());
                // Fall back to a simpler approach if the time series package is not available
                return predictUsingSimpleRegression(historicalCTR, days);
            }
        } catch (Exception e) {
            logger.error("Error making CTR predictions", e);
            return generateFallbackPredictions(new HashMap<>(), days);
        }
    }

    /**
     * Uses Weka's TimeSeriesForecasting package to predict future CTR values.
     *
     * @param historicalCTR The historical CTR data
     * @param days          The number of days to predict
     * @return A map of dates to predicted CTR values
     */
    Map<String, Double> predictUsingTimeSeriesForecasting(Map<LocalDate, Double> historicalCTR, int days) {
        try {
            if (historicalCTR.isEmpty()) {
                logger.warn("No historical data provided for time series forecasting");
                throw new RuntimeException("No historical data available");
            }
            
            // Use the regression data format which uses numeric day offsets instead of date strings
            // This is more compatible with Weka's forecasting capabilities
            Instances trainingData = prepareTrainingDataForRegression(historicalCTR);
            
            if (trainingData.isEmpty()) {
                logger.warn("Empty training dataset created");
                throw new RuntimeException("Empty training dataset");
            }
            
            // Set up and train the forecaster
            WekaForecaster forecaster = new WekaForecaster();
            forecaster.setFieldsToForecast("ctr");
            
            // Configure the forecaster for numeric time indicators
            forecaster.getTSLagMaker().setTimeStampField("day_offset"); // Use day_offset as time indicator
            forecaster.getTSLagMaker().setMinLag(1);
            forecaster.getTSLagMaker().setMaxLag(7); // Use up to 7 days of lag
            
            // Use linear regression as the base forecaster
            forecaster.setBaseForecaster(new LinearRegression());
            
            // Train with the prepared data
            logger.info("Building forecaster with {} instances", trainingData.size());
            forecaster.buildForecaster(trainingData);
            
            // Prime the forecaster with the same data used for training
            logger.info("Priming forecaster with training data");
            forecaster.primeForecaster(trainingData);
            
            // Make forecast
            logger.info("Forecasting for the next {} days", days);
            List<List<NumericPrediction>> forecast = forecaster.forecast(days);
            
            // Convert the forecast to a map of date -> CTR
            Map<String, Double> predictions = new HashMap<>();
            LocalDate currentDate = LocalDate.now();
            
            for (int i = 0; i < forecast.size(); i++) {
                List<NumericPrediction> predsAtStep = forecast.get(i);
                if (predsAtStep.isEmpty()) {
                    logger.warn("No prediction available for day {}", i+1);
                    continue;
                }
                
                NumericPrediction predForTarget = predsAtStep.get(0); // CTR is our only target
                LocalDate forecastDate = currentDate.plusDays(i + 1);
                
                double predictedCTR = predForTarget.predicted();
                
                // Ensure CTR is between 0 and 1
                predictedCTR = Math.max(0, Math.min(1, predictedCTR));
                
                predictions.put(forecastDate.format(DATE_FORMATTER), predictedCTR);
            }
            
            // If no predictions were generated, throw an exception to fall back to simple regression
            if (predictions.isEmpty()) {
                throw new RuntimeException("No predictions generated by forecaster");
            }
            
            return predictions;
        } catch (Exception e) {
            logger.warn("Error using time series forecasting: {}", e.getMessage());
            throw new RuntimeException("Time series forecasting unavailable", e);
        }
    }

    /**
     * Uses a simple linear regression to predict future CTR values.
     * This is used as a fallback when the time series forecasting package is not available.
     *
     * @param historicalCTR The historical CTR data
     * @param days          The number of days to predict
     * @return A map of dates to predicted CTR values
     */
    private Map<String, Double> predictUsingSimpleRegression(Map<LocalDate, Double> historicalCTR, int days) {
        try {
            // Convert historical data to Weka instances for simple regression
            Instances trainingData = prepareTrainingDataForRegression(historicalCTR);
            
            // Train the linear regression model
            LinearRegression regression = new LinearRegression();
            regression.buildClassifier(trainingData);
            
            // Make predictions for future days
            Map<String, Double> predictions = new HashMap<>();
            LocalDate currentDate = LocalDate.now();
            
            for (int i = 1; i <= days; i++) {
                LocalDate forecastDate = currentDate.plusDays(i);
                
                // Create test instance
                Instance testInstance = new DenseInstance(2);
                testInstance.setValue(0, i); // Day offset
                
                // Set dataset for the instance
                testInstance.setDataset(trainingData);
                
                // Make prediction
                double predictedCTR = regression.classifyInstance(testInstance);
                
                // Ensure CTR is between 0 and 1
                predictedCTR = Math.max(0, Math.min(1, predictedCTR));
                
                predictions.put(forecastDate.format(DATE_FORMATTER), predictedCTR);
            }
            
            return predictions;
        } catch (Exception e) {
            logger.error("Error using simple regression for prediction", e);
            return generateFallbackPredictions(historicalCTR, days);
        }
    }

    /**
     * Prepares Weka instances from historical CTR data for training the forecaster.
     *
     * @param historicalCTR The historical CTR data
     * @return Weka instances for training
     */
    private Instances prepareTrainingData(Map<LocalDate, Double> historicalCTR) {
        // Define attributes for the dataset
        ArrayList<Attribute> attributes = new ArrayList<>();
        // Create string attribute for date with ArrayList of possible values
        ArrayList<String> dateValues = new ArrayList<>();
        historicalCTR.keySet().stream()
                .sorted()
                .forEach(date -> dateValues.add(date.format(DATE_FORMATTER)));
        
        Attribute dateAttr = new Attribute("date", dateValues);
        attributes.add(dateAttr);
        attributes.add(new Attribute("ctr"));
        
        // Create dataset with attributes
        Instances dataset = new Instances("CTRData", attributes, historicalCTR.size());
        dataset.setClass(attributes.get(1)); // Set CTR as the class attribute
        
        // Add instances to the dataset
        List<LocalDate> sortedDates = historicalCTR.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
        
        for (LocalDate date : sortedDates) {
            double ctr = historicalCTR.get(date);
            
            // Create instance with proper dataset reference
            Instance instance = new DenseInstance(2);
            // Set dataset first
            instance.setDataset(dataset);
            // Then set values
            instance.setValue(0, date.format(DATE_FORMATTER));
            instance.setValue(1, ctr);
            
            dataset.add(instance);
        }
        
        return dataset;
    }

    /**
     * Prepares Weka instances from historical CTR data for simple regression.
     * Uses day offset as feature instead of date string.
     *
     * @param historicalCTR The historical CTR data
     * @return Weka instances for training simple regression
     */
    private Instances prepareTrainingDataForRegression(Map<LocalDate, Double> historicalCTR) {
        // Define attributes for the dataset
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("day_offset"));
        attributes.add(new Attribute("ctr"));
        
        // Create dataset with attributes
        Instances dataset = new Instances("CTRRegressionData", attributes, historicalCTR.size());
        dataset.setClass(attributes.get(1)); // Set CTR as the class attribute
        
        // Sort dates
        List<LocalDate> sortedDates = historicalCTR.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
        
        // Find earliest date to calculate offsets
        if (sortedDates.isEmpty()) {
            return dataset;
        }
        
        LocalDate earliestDate = sortedDates.get(0);
        
        // Add instances to the dataset
        for (LocalDate date : sortedDates) {
            double ctr = historicalCTR.get(date);
            int dayOffset = (int) (date.toEpochDay() - earliestDate.toEpochDay());
            
            // Create instance with proper dataset reference
            Instance instance = new DenseInstance(2);
            // Set dataset first
            instance.setDataset(dataset);
            // Then set values
            instance.setValue(0, dayOffset);
            instance.setValue(1, ctr);
            
            dataset.add(instance);
        }
        
        return dataset;
    }

    /**
     * Gets historical click-through rate data for a given store.
     *
     * @param storeId The ID of the store
     * @return A map of dates to CTR values
     */
    private Map<LocalDate, Double> getHistoricalCTR(Long storeId) {
        // Get sent messages grouped by date
        Integer storeIdInt = storeId.intValue();
        Map<LocalDate, Long> messagesByDate = sentTextMessageRepository.findByStore_Id(storeIdInt).stream()
                .collect(Collectors.groupingBy(
                        msg -> msg.getSentAt().toLocalDate(),
                        Collectors.counting()
                ));
        
        // Get clicks grouped by date
        Map<LocalDate, Long> clicksByDate = linkClickRepository.findByStoreId(storeIdInt).stream()
                .collect(Collectors.groupingBy(
                        click -> click.getClickedAt().toLocalDate(),
                        Collectors.counting()
                ));
        
        // Calculate CTR for each date
        Map<LocalDate, Double> ctrByDate = new HashMap<>();
        for (Map.Entry<LocalDate, Long> entry : messagesByDate.entrySet()) {
            LocalDate date = entry.getKey();
            long messages = entry.getValue();
            long clicks = clicksByDate.getOrDefault(date, 0L);
            
            if (messages > 0) {
                double ctr = (double) clicks / messages;
                ctrByDate.put(date, ctr);
            }
        }
        
        return ctrByDate;
    }

    /**
     * Generates fallback predictions when there's not enough historical data.
     * Uses a simple average or linear extrapolation based on available data.
     *
     * @param historicalCTR The historical CTR data
     * @param days          The number of days to predict
     * @return A map of dates to predicted CTR values
     */
    private Map<String, Double> generateFallbackPredictions(Map<LocalDate, Double> historicalCTR, int days) {
        double averageCTR = historicalCTR.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.05); // Default to 5% if no data available
        
        Map<String, Double> predictions = new HashMap<>();
        LocalDate currentDate = LocalDate.now();
        
        for (int i = 1; i <= days; i++) {
            LocalDate forecastDate = currentDate.plusDays(i);
            predictions.put(forecastDate.format(DATE_FORMATTER), averageCTR);
        }
        
        return predictions;
    }
} 