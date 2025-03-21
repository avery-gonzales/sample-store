package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.repository.LinkClickRepository;
import org.example.repository.SentTextMessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final SentTextMessageRepository sentTextMessageRepository;
    private final LinkClickRepository linkClickRepository;

    /**
     * Get analytics for a store using a Long ID
     * @param storeId The store ID as a Long
     * @return Analytics data as a map
     */
    public Map<String, Object> getStoreAnalytics(Long storeId) {
        return getStoreAnalytics(storeId.intValue());
    }

    public Map<String, Object> getStoreAnalytics(Integer storeId) {
        Map<String, Object> analytics = new HashMap<>();
        
        // Total messages sent
        Long totalMessages = sentTextMessageRepository.countMessagesByStore(storeId);
        
        // Total clicks
        Long totalClicks = linkClickRepository.countClicksByStore(storeId);
        
        // Click-through rate
        double ctr = totalMessages > 0 ? (double) totalClicks / totalMessages : 0;
        
        // Messages by month
        List<Map<String, Object>> messagesByMonth = sentTextMessageRepository.countMessagesByMonth(storeId)
                .stream()
                .map(row -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("month", row[0]);
                    item.put("year", row[1]);
                    item.put("count", row[2]);
                    return item;
                })
                .collect(Collectors.toList());
        
        // Clicks by month
        List<Map<String, Object>> clicksByMonth = linkClickRepository.countClicksByMonth(storeId)
                .stream()
                .map(row -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("month", row[0]);
                    item.put("year", row[1]);
                    item.put("count", row[2]);
                    return item;
                })
                .collect(Collectors.toList());
        
        // Templates performance
        Map<Integer, Long> messagesByTemplate = new HashMap<>();
        sentTextMessageRepository.countMessagesByTemplate(storeId)
                .forEach(row -> messagesByTemplate.put((Integer) row[0], (Long) row[1]));
        
        Map<Integer, Long> clicksByTemplate = new HashMap<>();
        linkClickRepository.countClicksByTemplate(storeId)
                .forEach(row -> clicksByTemplate.put((Integer) row[0], (Long) row[1]));
        
        // Calculate CTR by template
        Map<Integer, Double> ctrByTemplate = new HashMap<>();
        messagesByTemplate.forEach((templateId, messageCount) -> {
            Long clickCount = clicksByTemplate.getOrDefault(templateId, 0L);
            double templateCtr = messageCount > 0 ? (double) clickCount / messageCount : 0;
            ctrByTemplate.put(templateId, templateCtr);
        });
        
        analytics.put("totalMessages", totalMessages);
        analytics.put("totalClicks", totalClicks);
        analytics.put("clickThroughRate", ctr);
        analytics.put("messagesByMonth", messagesByMonth);
        analytics.put("clicksByMonth", clicksByMonth);
        analytics.put("messagesByTemplate", messagesByTemplate);
        analytics.put("clicksByTemplate", clicksByTemplate);
        analytics.put("ctrByTemplate", ctrByTemplate);
        
        return analytics;
    }
    
    /**
     * Get recent analytics for a store using a Long ID
     * @param storeId The store ID as a Long
     * @param days Number of days to include
     * @return Recent analytics data as a map
     */
    public Map<String, Object> getRecentAnalytics(Long storeId, int days) {
        return getRecentAnalytics(storeId.intValue(), days);
    }
    
    public Map<String, Object> getRecentAnalytics(Integer storeId, int days) {
        Map<String, Object> recentAnalytics = new HashMap<>();
        
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        Long recentMessages = sentTextMessageRepository.countMessagesByStoreAndDateRange(storeId, startDate, endDate);
        
        recentAnalytics.put("period", days + " days");
        recentAnalytics.put("recentMessages", recentMessages);
        
        return recentAnalytics;
    }
} 