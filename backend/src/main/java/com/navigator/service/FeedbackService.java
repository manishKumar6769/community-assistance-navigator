package com.navigator.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.navigator.model.Feedback;
import com.navigator.model.Referral;
import com.navigator.model.Resource;
import com.navigator.repository.FeedbackRepository;
import com.navigator.repository.ReferralRepository;
import com.navigator.repository.ResourceRepository;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final ReferralRepository referralRepository;
    private final ResourceRepository resourceRepository;

    public FeedbackService(FeedbackRepository feedbackRepository,
                           ReferralRepository referralRepository,
                           ResourceRepository resourceRepository) {
        this.feedbackRepository = feedbackRepository;
        this.referralRepository = referralRepository;
        this.resourceRepository = resourceRepository;
    }

    /** Returns null if the referral does not exist. */
    public Feedback submit(Feedback input) {
        Referral referral = referralRepository.findById(input.getReferralId());
        if (referral == null) {
            return null;
        }
        input.setFeedbackId(UUID.randomUUID().toString());
        input.setResourceId(referral.getResourceId());
        input.setCreatedAt(Instant.now().toString());
        Feedback saved = feedbackRepository.save(input);

        updateReliability(referral.getResourceId(), input.getRating());
        return saved;
    }

    private void updateReliability(String resourceId, int rating) {
        Resource resource = resourceRepository.findById(resourceId);
        if (resource == null) {
            return; // referral points to a resource that is not in the table
        }
        int score = resource.getReliabilityScore() == null ? 50 : resource.getReliabilityScore();
        if (rating >= 4) {
            score += 2;
        } else if (rating <= 2) {
            score -= 3;
        }
        score = Math.max(0, Math.min(100, score));
        resource.setReliabilityScore(score);
        resourceRepository.save(resource);
    }
}