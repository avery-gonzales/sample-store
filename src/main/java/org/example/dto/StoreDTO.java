package org.example.dto;

import lombok.Data;
import org.example.model.Store;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StoreDTO {
    private Integer id;
    private String name;
    private BigDecimal lat;
    private BigDecimal lng;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
    private OrganizationDTO organization;
    
    public static StoreDTO fromEntity(Store store) {
        StoreDTO dto = new StoreDTO();
        dto.setId(store.getId());
        dto.setName(store.getName());
        dto.setLat(store.getLat());
        dto.setLng(store.getLng());
        dto.setCreatedAt(store.getCreatedAt());
        dto.setLastModifiedAt(store.getLastModifiedAt());
        
        if (store.getOrganization() != null) {
            dto.setOrganization(OrganizationDTO.fromEntityWithoutStores(store.getOrganization()));
        }
        
        return dto;
    }
    
    public static StoreDTO fromEntityWithoutOrganization(Store store) {
        StoreDTO dto = new StoreDTO();
        dto.setId(store.getId());
        dto.setName(store.getName());
        dto.setLat(store.getLat());
        dto.setLng(store.getLng());
        dto.setCreatedAt(store.getCreatedAt());
        dto.setLastModifiedAt(store.getLastModifiedAt());
        return dto;
    }
} 