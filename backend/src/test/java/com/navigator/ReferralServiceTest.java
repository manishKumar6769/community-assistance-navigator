package com.navigator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.navigator.model.Referral;
import com.navigator.repository.ReferralRepository;
import com.navigator.service.ReferralService;

class ReferralServiceTest {

    private final ReferralRepository repository = mock(ReferralRepository.class);
    private final ReferralService service = new ReferralService(repository);

    @Test
    void createStartsWithRequestedStatus() {
        Referral input = new Referral();
        input.setUserId("user-1");
        input.setResourceId("resource-1");
        input.setAssessmentId("assessment-1");
        when(repository.save(input)).thenReturn(input);

        Referral saved = service.create(input);

        assertEquals("REQUESTED", saved.getStatus());
        assertNull(saved.getFailureReason());
        assertTrue(saved.getReferralId() != null);
        assertTrue(saved.getCreatedAt() != null);
        assertTrue(saved.getUpdatedAt() != null);
    }

    @Test
    void notReceivedRequiresFailureReason() {
        when(repository.findById("ref-1")).thenReturn(existingReferral());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateStatus("ref-1", "NOT_RECEIVED", null)
        );
    }

    @Test
    void receivedClearsFailureReason() {
        Referral referral = existingReferral();
        referral.setFailureReason("NO_RESPONSE");

        when(repository.findById("ref-1")).thenReturn(referral);
        when(repository.save(referral)).thenReturn(referral);

        Referral updated = service.updateStatus("ref-1", "RECEIVED", "NO_RESPONSE");

        assertEquals("RECEIVED", updated.getStatus());
        assertNull(updated.getFailureReason());
    }

    private Referral existingReferral() {
        Referral referral = new Referral();
        referral.setReferralId("ref-1");
        referral.setUserId("user-1");
        referral.setResourceId("resource-1");
        referral.setAssessmentId("assessment-1");
        referral.setStatus("REQUESTED");
        return referral;
    }
}