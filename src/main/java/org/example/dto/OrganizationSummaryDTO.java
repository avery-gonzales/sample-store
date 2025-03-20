package org.example.dto;

import lombok.Data;
import org.example.model.Organization;

@Data
public class OrganizationSummaryDTO {
    private Integer id;
    private String name;
    
    public static OrganizationSummaryDTO fromEntity(Organization organization) {
        OrganizationSummaryDTO dto = new OrganizationSummaryDTO();
        dto.setId(organization.getId());
        dto.setName(organization.getName());
        return dto;
    }
} 