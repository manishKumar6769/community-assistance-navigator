package com.navigator;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.navigator.matching.MatchingEngine;
import com.navigator.model.Assessment;
import com.navigator.model.Resource;

class MatchingEngineTest {

    private final MatchingEngine engine = new MatchingEngine();

    private Assessment assessment() {
        Assessment a = new Assessment();
        a.setAge(20);
        a.setIsStudent(true);
        a.setIncomeLevel("LOW");
        a.setLocation("Jamshedpur");
        return a;
    }

    private Resource resource(String location, String availability, int reliability) {
        Resource r = new Resource();
        r.setLocation(location);
        r.setMinAge(15);
        r.setMaxAge(30);
        r.setStudentOnly(true);
        r.setMaxIncomeLevel("MEDIUM");
        r.setAvailability(availability);
        r.setReliabilityScore(reliability);
        return r;
    }

    @Test
    void nearbyOpenReliableResourceScoresHigher() {
        int good = engine.score(assessment(), resource("Jamshedpur", "OPEN", 90));
        int weak = engine.score(assessment(), resource("Ranchi", "CLOSED", 30));
        assertTrue(good > weak);
    }

    @Test
    void scoreStaysBetweenZeroAndHundred() {
        int s = engine.score(assessment(), resource("Jamshedpur", "OPEN", 100));
        assertTrue(s >= 0 && s <= 100);
    }

    @Test
    void matchingPreferenceScoresHigher() {
        Assessment a = assessment();
        a.setPreference("food");

        Resource matching = resource("Jamshedpur", "OPEN", 90);
        matching.setServices("Food assistance and groceries");

        Resource nonMatching = resource("Jamshedpur", "OPEN", 90);
        nonMatching.setServices("Education support");

        int good = engine.score(a, matching);
        int weak = engine.score(a, nonMatching);

        assertTrue(good > weak);
    }

    @Test
    void ineligibleResourceIsRejected() {
        Assessment a = assessment();
        a.setIsStudent(false);

        Resource r = resource("Jamshedpur", "OPEN", 90);
        r.setStudentOnly(true);

        assertTrue(!engine.isEligible(a, r));
    }

    @Test
    void reliabilityAffectsScore() {
        Assessment a = assessment();

        Resource reliable = resource("Jamshedpur", "OPEN", 90);
        Resource unreliable = resource("Jamshedpur", "OPEN", 30);

        int good = engine.score(a, reliable);
        int weak = engine.score(a, unreliable);

        assertTrue(good > weak);
    }

    @Test
    void freshVerifiedResourceScoresHigherThanOldUnverifiedResource() {
        Assessment a = assessment();

        Resource fresh = resource("Jamshedpur", "OPEN", 90);
        fresh.setLastVerifiedDate(java.time.LocalDate.now().toString());
        fresh.setVerificationStatus("VERIFIED");

        Resource old = resource("Jamshedpur", "OPEN", 90);
        old.setLastVerifiedDate(java.time.LocalDate.now().minusDays(200).toString());
        old.setVerificationStatus("UNVERIFIED");

        int freshScore = engine.score(a, fresh);
        int oldScore = engine.score(a, old);

        assertTrue(freshScore > oldScore);
    }
}