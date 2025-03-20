package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.OrganizationDTO;
import org.example.dto.OrganizationSummaryDTO;
import org.example.model.Organization;
import org.example.service.OrganizationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationService organizationService;

    @GetMapping
    public ResponseEntity<Page<OrganizationSummaryDTO>> getAllOrganizations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrganizationSummaryDTO> organizationDTOs = organizationService.getAllOrganizationsPaged(pageable);
        return ResponseEntity.ok(organizationDTOs);
    }
    
    @GetMapping("/list")
    public ResponseEntity<List<OrganizationSummaryDTO>> getOrganizationsList() {
        List<Organization> organizations = organizationService.getAllOrganizations();
        List<OrganizationSummaryDTO> dtos = organizations.stream()
                .map(OrganizationSummaryDTO::fromEntity)
                .collect(Collectors.toList());
        
        // Force evaluation and convert to simple ArrayList to avoid serialization issues
        return ResponseEntity.ok(new ArrayList<>(dtos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationDTO> getOrganizationById(@PathVariable Integer id) {
        Organization organization = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(OrganizationDTO.fromEntity(organization));
    }

    /**
     * Simple endpoint designed specifically for dropdowns
     * Returns a basic array of {id, name} objects without any wrappers
     */
    @GetMapping("/dropdown")
    public ResponseEntity<List<Map<String, Object>>> getOrganizationsForDropdown() {
        List<Organization> organizations = organizationService.getAllOrganizations();
        List<Map<String, Object>> simpleList = new ArrayList<>();
        
        for (Organization org : organizations) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", org.getId());
            map.put("name", org.getName());
            simpleList.add(map);
        }
        
        return ResponseEntity.ok(simpleList);
    }
} 