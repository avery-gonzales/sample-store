package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.OrganizationSummaryDTO;
import org.example.model.Organization;
import org.example.repository.OrganizationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    private final OrganizationRepository organizationRepository;

    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }
    
    public Page<OrganizationSummaryDTO> getAllOrganizationsPaged(Pageable pageable) {
        return organizationRepository.findAll(pageable)
                .map(OrganizationSummaryDTO::fromEntity);
    }

    public Organization getOrganizationById(Integer id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found with id: " + id));
    }
} 