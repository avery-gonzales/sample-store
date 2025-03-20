package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.StoreDTO;
import org.example.model.Store;
import org.example.service.StoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;

    @GetMapping
    public ResponseEntity<List<StoreDTO>> getAllStores() {
        List<Store> stores = storeService.getAllStores();
        List<StoreDTO> storeDTOs = stores.stream()
                .map(StoreDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(storeDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreDTO> getStoreById(@PathVariable Integer id) {
        Store store = storeService.getStoreById(id);
        return ResponseEntity.ok(StoreDTO.fromEntity(store));
    }

    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<List<StoreDTO>> getStoresByOrganizationId(@PathVariable Integer organizationId) {
        List<Store> stores = storeService.getStoresByOrganizationId(organizationId);
        List<StoreDTO> storeDTOs = stores.stream()
                .map(StoreDTO::fromEntity)
                .collect(Collectors.toList());
        
        // Force evaluation and convert to simple ArrayList to avoid serialization issues
        return ResponseEntity.ok(new ArrayList<>(storeDTOs));
    }

    /**
     * Simple endpoint designed specifically for dropdowns
     * Returns a basic array of {id, name} objects without any wrappers
     */
    @GetMapping("/organization/{organizationId}/dropdown")
    public ResponseEntity<List<Map<String, Object>>> getStoresByOrganizationIdForDropdown(
            @PathVariable Integer organizationId) {
        List<Store> stores = storeService.getStoresByOrganizationId(organizationId);
        List<Map<String, Object>> simpleList = new ArrayList<>();
        
        for (Store store : stores) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", store.getId());
            map.put("name", store.getName());
            simpleList.add(map);
        }
        
        return ResponseEntity.ok(simpleList);
    }
} 