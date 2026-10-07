package com.navigator.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.navigator.matching.MatchingEngine;
import com.navigator.model.Assessment;
import com.navigator.model.Resource;
import com.navigator.repository.AssessmentRepository;
import com.navigator.repository.ResourceRepository;

@Service
public class RecommendationService {

    private final AssessmentRepository assessmentRepository;
    private final ResourceRepository resourceRepository;
    private final MatchingEngine engine;

    public RecommendationService(AssessmentRepository assessmentRepository,
            ResourceRepository resourceRepository,
            MatchingEngine engine) {
        this.assessmentRepository = assessmentRepository;
        this.resourceRepository = resourceRepository;
        this.engine = engine;
    }

    /** Returns null if the assessment does not exist. */
    public List<Map<String, Object>> recommend(String assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId);
        if (assessment == null) {
            return null;
        }
        List<Resource> candidates = resourceRepository.findByCategory(assessment.getNeedCategory());

        List<Map<String, Object>> ranked = new ArrayList<>();
        for (Resource r : candidates) {
            if (!engine.isEligible(assessment, r)) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("matchScore", engine.score(assessment, r));
            item.put("resource", r);
            ranked.add(item);
        }
        ranked.sort(Comparator.comparingInt((Map<String, Object> m) -> (Integer) m.get("matchScore")).reversed());
        return ranked;
    }
}