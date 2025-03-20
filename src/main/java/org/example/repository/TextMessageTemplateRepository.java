package org.example.repository;

import org.example.model.TextMessageTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TextMessageTemplateRepository extends JpaRepository<TextMessageTemplate, Integer> {
    List<TextMessageTemplate> findByStoreId(Integer storeId);
} 