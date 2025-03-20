package org.example.repository;

import org.example.model.LinkClick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LinkClickRepository extends JpaRepository<LinkClick, Integer> {
    
    @Query("SELECT COUNT(lc) FROM LinkClick lc JOIN lc.sentTextMessage stm WHERE stm.store.id = :storeId")
    Long countClicksByStore(@Param("storeId") Integer storeId);
    
    @Query("SELECT stm.template.id, COUNT(lc) FROM LinkClick lc JOIN lc.sentTextMessage stm " +
           "WHERE stm.store.id = :storeId GROUP BY stm.template.id")
    List<Object[]> countClicksByTemplate(@Param("storeId") Integer storeId);
    
    @Query(value = "SELECT EXTRACT(MONTH FROM lc.clicked_at) as month, " +
                  "EXTRACT(YEAR FROM lc.clicked_at) as year, COUNT(*) as count " +
                  "FROM link_click lc " +
                  "JOIN sent_text_message stm ON lc.sent_text_message_id = stm.id " +
                  "WHERE stm.store_id = :storeId " +
                  "GROUP BY EXTRACT(YEAR FROM lc.clicked_at), EXTRACT(MONTH FROM lc.clicked_at) " +
                  "ORDER BY year, month", nativeQuery = true)
    List<Object[]> countClicksByMonth(@Param("storeId") Integer storeId);
    
    @Query("SELECT COUNT(lc) FROM LinkClick lc JOIN lc.sentTextMessage stm WHERE stm.id = :messageId")
    Long countClicksForMessage(@Param("messageId") Integer messageId);
} 