package com.navigator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.navigator.model.Feedback;
import com.navigator.model.Referral;
import com.navigator.model.Resource;
import com.navigator.repository.FeedbackRepository;
import com.navigator.repository.ReferralRepository;
import com.navigator.repository.ResourceRepository;
import com.navigator.service.FeedbackService;

class FeedbackServiceTest {

    private final FeedbackRepository feedbackRepository =
            mock(FeedbackRepository.class);

    private final ReferralRepository referralRepository =
            mock(ReferralRepository.class);

    private final ResourceRepository resourceRepository =
            mock(ResourceRepository.class);

    private final FeedbackService service =
            new FeedbackService(
                    feedbackRepository,
                    referralRepository,
                    resourceRepository
            );

    @Test
    void highRatingIncreasesReliability() {
        Referral referral = new Referral();
        referral.setReferralId("ref-1");
        referral.setResourceId("resource-1");

        Resource resource = new Resource();
        resource.setResourceId("resource-1");
        resource.setReliabilityScore(80);

        Feedback feedback = new Feedback();
        feedback.setReferralId("ref-1");
        feedback.setRating(5);

        when(referralRepository.findById("ref-1")).thenReturn(referral);
        when(feedbackRepository.save(feedback)).thenReturn(feedback);
        when(resourceRepository.findById("resource-1")).thenReturn(resource);
        when(resourceRepository.save(resource)).thenReturn(resource);

        Feedback saved = service.submit(feedback);

        assertNotNull(saved.getFeedbackId());
        assertNotNull(saved.getCreatedAt());
        assertEquals("resource-1", saved.getResourceId());
        assertEquals(82, resource.getReliabilityScore());
    }

    @Test
    void lowRatingDecreasesReliability() {
        Referral referral = new Referral();
        referral.setReferralId("ref-1");
        referral.setResourceId("resource-1");

        Resource resource = new Resource();
        resource.setResourceId("resource-1");
        resource.setReliabilityScore(80);

        Feedback feedback = new Feedback();
        feedback.setReferralId("ref-1");
        feedback.setRating(2);

        when(referralRepository.findById("ref-1")).thenReturn(referral);
        when(feedbackRepository.save(feedback)).thenReturn(feedback);
        when(resourceRepository.findById("resource-1")).thenReturn(resource);
        when(resourceRepository.save(resource)).thenReturn(resource);

        service.submit(feedback);

        assertEquals(77, resource.getReliabilityScore());
    }
}