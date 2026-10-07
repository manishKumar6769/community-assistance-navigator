package com.navigator.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.navigator.model.Resource;
import com.navigator.repository.ResourceRepository;

@Service
public class ResourceService {

    private final ResourceRepository repository;

    public ResourceService(ResourceRepository repository) {
        this.repository = repository;
    }

    public Resource create(Resource input) {
        input.setResourceId(UUID.randomUUID().toString());
        if (input.getLastVerifiedDate() == null) {
            input.setLastVerifiedDate(LocalDate.now().toString());
        }
        if (input.getReliabilityScore() == null) {
            input.setReliabilityScore(50);
        }
        if (input.getAvailability() == null) {
            input.setAvailability("OPEN");
        }
        if (input.getVerificationStatus() == null) {
            input.setVerificationStatus("UNVERIFIED");
        }
        return repository.save(input);
    }

    public Resource get(String id) {
        return repository.findById(id);
    }

    public List<Resource> list(String category) {
        if (category == null || category.isBlank()) {
            return repository.findAll();
        }
        return repository.findByCategory(category);
    }
}