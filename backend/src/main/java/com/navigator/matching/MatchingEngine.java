package com.navigator.matching;

import org.springframework.stereotype.Component;

import com.navigator.model.Assessment;
import com.navigator.model.Resource;

/**
 * Simple first version. Member 4 can improve or replace this class.
 * Weights: Eligibility 30, Location 25, Availability 20, Reliability 15, Preference 10.
 */
@Component
public class MatchingEngine {

    public int score(Assessment a, Resource r) {
        double total = 0;
        total += eligibility(a, r) * 30;
        total += location(a, r) * 25;
        total += availability(r) * 20;
        total += reliability(r) * 15;
        total += preference(a, r) * 10;
        return (int) Math.round(total);
    }

    /** Returns 0.0 to 1.0 */
    private double eligibility(Assessment a, Resource r) {
        double points = 0;
        int checks = 0;

        if (a.getAge() != null && r.getMinAge() != null && r.getMaxAge() != null) {
            checks++;
            if (a.getAge() >= r.getMinAge() && a.getAge() <= r.getMaxAge()) {
                points++;
            }
        }
        if (Boolean.TRUE.equals(r.getStudentOnly())) {
            checks++;
            if (Boolean.TRUE.equals(a.getIsStudent())) {
                points++;
            }
        }
        if (a.getIncomeLevel() != null && r.getMaxIncomeLevel() != null) {
            checks++;
            if (incomeRank(a.getIncomeLevel()) <= incomeRank(r.getMaxIncomeLevel())) {
                points++;
            }
        }
        return checks == 0 ? 1.0 : points / checks;
    }

    private int incomeRank(String level) {
        switch (level.toUpperCase()) {
            case "LOW": return 1;
            case "MEDIUM": return 2;
            default: return 3;
        }
    }

    private double location(Assessment a, Resource r) {
        if (a.getLocation() == null || r.getLocation() == null) return 0;
        return a.getLocation().equalsIgnoreCase(r.getLocation()) ? 1.0 : 0.0;
    }

    private double availability(Resource r) {
        if (r.getAvailability() == null) return 0.5;
        switch (r.getAvailability().toUpperCase()) {
            case "OPEN": return 1.0;
            case "LIMITED": return 0.5;
            default: return 0.0;
        }
    }

    private double reliability(Resource r) {
        int s = r.getReliabilityScore() == null ? 50 : r.getReliabilityScore();
        return Math.max(0, Math.min(100, s)) / 100.0;
    }

    private double preference(Assessment a, Resource r) {
        // Placeholder: full marks for now. Member 4 can refine this.
        return 1.0;
    }
}