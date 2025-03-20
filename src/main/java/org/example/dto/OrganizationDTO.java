package org.example.dto;

import lombok.Data;
import org.example.model.Organization;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrganizationDTO {
    private Integer id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
    private List<StoreDTO> stores;
    
    public static OrganizationDTO fromEntity(Organization organization) {
        OrganizationDTO dto = new OrganizationDTO();
        dto.setId(organization.getId());
        dto.setName(organization.getName());
        dto.setCreatedAt(organization.getCreatedAt());
        dto.setLastModifiedAt(organization.getLastModifiedAt());
        
        if (organization.getStores() != null) {
            dto.setStores(organization.getStores().stream()
                    .map(StoreDTO::fromEntityWithoutOrganization)
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    public static OrganizationDTO fromEntityWithoutStores(Organization organization) {
        OrganizationDTO dto = new OrganizationDTO();
        dto.setId(organization.getId());
        dto.setName(organization.getName());
        dto.setCreatedAt(organization.getCreatedAt());
        dto.setLastModifiedAt(organization.getLastModifiedAt());
        return dto;
    }
} 