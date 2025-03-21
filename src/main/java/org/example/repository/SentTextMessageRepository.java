package org.example.repository;

import org.example.model.SentTextMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SentTextMessageRepository extends JpaRepository<SentTextMessage, Integer> {
    List<SentTextMessage> findByStore_Id(Integer storeId);
    
    List<SentTextMessage> findByStore_Id(Long storeId);
    
    List<SentTextMessage> findByClientId(Integer clientId);
    
    @Query("SELECT COUNT(s) FROM SentTextMessage s WHERE s.store.id = :storeId")
    Long countMessagesByStore(@Param("storeId") Integer storeId);
    
    @Query("SELECT COUNT(s) FROM SentTextMessage s WHERE s.store.id = :storeId AND s.sentAt BETWEEN :startDate AND :endDate")
    Long countMessagesByStoreAndDateRange(
            @Param("storeId") Integer storeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT s.template.id, COUNT(s) FROM SentTextMessage s WHERE s.store.id = :storeId GROUP BY s.template.id")
    List<Object[]> countMessagesByTemplate(@Param("storeId") Integer storeId);
    
    @Query(value = "SELECT EXTRACT(MONTH FROM sent_at) as month, EXTRACT(YEAR FROM sent_at) as year, COUNT(*) as count " +
                 "FROM sent_text_message WHERE store_id = :storeId " +
                 "GROUP BY EXTRACT(YEAR FROM sent_at), EXTRACT(MONTH FROM sent_at) " +
                 "ORDER BY year, month", nativeQuery = true)
    List<Object[]> countMessagesByMonth(@Param("storeId") Integer storeId);
} 