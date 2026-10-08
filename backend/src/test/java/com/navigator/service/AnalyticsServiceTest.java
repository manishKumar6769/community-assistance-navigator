package com.navigator.service;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.navigator.model.Referral;
import com.navigator.repository.ReferralRepository;

class AnalyticsServiceTest {

    private final ReferralRepository referralRepository = mock(ReferralRepository.class);
    private final AnalyticsService service = new AnalyticsService(referralRepository);

    @Test
    void summaryCountsStatusesAndFailureReasons() {
        when(referralRepository.findAll()).thenReturn(List.of(
                referral("REQUESTED", null),
                referral("CONTACTED", null),
                referral("RECEIVED", null),
                referral("NOT_RECEIVED", "NO_RESPONSE"),
                referral("NOT_RECEIVED", "TOO_FAR")
        ));

        Map<String, Object> summary = service.summary();
        Map<String, Integer> failureReasons = failureReasons(summary);

        assertEquals(5, summary.get("totalReferrals"));
        assertEquals(1, summary.get("requested"));
        assertEquals(1, summary.get("contacted"));
        assertEquals(1, summary.get("received"));
        assertEquals(2, summary.get("notReceived"));
        assertEquals(33.3, summary.get("successRatePercent"));
        assertEquals(1, failureReasons.get("NO_RESPONSE"));
        assertEquals(1, failureReasons.get("TOO_FAR"));
    }

    @Test
    void summaryReturnsZeroCountsWhenNoReferralsExist() {
        when(referralRepository.findAll()).thenReturn(List.of());

        Map<String, Object> summary = service.summary();
        Map<String, Integer> failureReasons = failureReasons(summary);

        assertEquals(0, summary.get("totalReferrals"));
        assertEquals(0, summary.get("requested"));
        assertEquals(0, summary.get("contacted"));
        assertEquals(0, summary.get("received"));
        assertEquals(0, summary.get("notReceived"));
        assertEquals(0.0, summary.get("successRatePercent"));
        assertTrue(failureReasons.isEmpty());
    }

    private Referral referral(String status, String failureReason) {
        Referral referral = new Referral();
        referral.setStatus(status);
        referral.setFailureReason(failureReason);
        return referral;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Integer> failureReasons(Map<String, Object> summary) {
        return (Map<String, Integer>) summary.get("failureReasons");
    }
}
