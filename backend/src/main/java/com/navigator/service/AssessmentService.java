package com.navigator.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.navigator.model.Assessment;
import com.navigator.repository.AssessmentRepository;

@Service
public class AssessmentService {

    private final AssessmentRepository repository;

    public AssessmentService(AssessmentRepository repository) {
        this.repository = repository;
    }

    public Assessment create(Assessment input) {
        input.setAssessmentId(UUID.randomUUID().toString());
        input.setCreatedAt(Instant.now().toString());
        return repository.save(input);
    }

    public Assessment get(String assessmentId) {
        return repository.findById(assessmentId);
    }
}