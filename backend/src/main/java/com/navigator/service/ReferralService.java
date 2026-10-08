package com.navigator.service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.navigator.model.Referral;
import com.navigator.model.Resource;
import com.navigator.repository.ReferralRepository;
import com.navigator.repository.ResourceRepository;

@Service
public class ReferralService {

    private static final Set<String> STATUSES = Set.of("REQUESTED", "CONTACTED", "RECEIVED", "NOT_RECEIVED");
    private static final Set<String> FAILURE_REASONS = Set.of("NO_RESPONSE", "NOT_ELIGIBLE", "TOO_FAR", "UNAVAILABLE",
            "INCORRECT_INFO");

    private final ReferralRepository repository;
    private final ResourceRepository resourceRepository;

    public ReferralService(ReferralRepository repository,
            ResourceRepository resourceRepository) {
        this.repository = repository;
        this.resourceRepository = resourceRepository;
    }

    public Referral create(Referral input) {
        String now = Instant.now().toString();
        input.setReferralId(UUID.randomUUID().toString());
        input.setStatus("REQUESTED");
        input.setFailureReason(null);
        input.setCreatedAt(now);
        input.setUpdatedAt(now);
        return repository.save(input);
    }

    public Referral get(String id) {
        return repository.findById(id);
    }

    public List<Referral> listByUser(String userId) {
        return repository.findByUserId(userId);
    }

    /**
     * Returns null if not found. Throws IllegalArgumentException for invalid input.
     */
    public Referral updateStatus(String id, String status, String failureReason) {
        if (status == null || !STATUSES.contains(status)) {
            throw new IllegalArgumentException(
                    "status must be one of REQUESTED, CONTACTED, RECEIVED, NOT_RECEIVED");
        }
        if ("NOT_RECEIVED".equals(status)) {
            if (failureReason == null || !FAILURE_REASONS.contains(failureReason)) {
                throw new IllegalArgumentException(
                        "failureReason is required for NOT_RECEIVED: NO_RESPONSE, NOT_ELIGIBLE, TOO_FAR, UNAVAILABLE or INCORRECT_INFO");
            }
        } else {
            failureReason = null;
        }
        Referral existing = repository.findById(id);
        if (existing == null) {
            return null;
        }
        existing.setStatus(status);
        existing.setFailureReason(failureReason);
        existing.setUpdatedAt(Instant.now().toString());

        Referral saved = repository.save(existing);
        updateResourceReliability(saved);

        return saved;
    }

    private void updateResourceReliability(Referral referral) {
        if (!"NOT_RECEIVED".equals(referral.getStatus())
                || !"INCORRECT_INFO".equals(referral.getFailureReason())) {
            return;
        }

        Resource resource = resourceRepository.findById(referral.getResourceId());

        if (resource == null) {
            return;
        }

        int score = resource.getReliabilityScore() == null
                ? 50
                : resource.getReliabilityScore();

        score -= 5;
        score = Math.max(0, score);

        resource.setReliabilityScore(score);
        resourceRepository.save(resource);
    }
}