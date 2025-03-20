package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.model.Store;
import org.example.repository.StoreRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;

    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }

    public Store getStoreById(Integer id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Store not found with id: " + id));
    }

    public List<Store> getStoresByOrganizationId(Integer organizationId) {
        return storeRepository.findByOrganizationId(organizationId);
    }
} 