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
}