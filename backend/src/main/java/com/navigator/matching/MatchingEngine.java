package com.navigator.matching;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import com.navigator.model.Assessment;
import com.navigator.model.Resource;

/**
 * Simple first version. Member 4 can improve or replace this class.
 * Weights: Eligibility 30, Location 25, Availability 20, Reliability 15,
 * Preference 10.
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

    public boolean isEligible(Assessment a, Resource r) {
        if (a.getAge() != null && r.getMinAge() != null
                && a.getAge() < r.getMinAge()) {
            return false;
        }
        if (a.getAge() != null && r.getMaxAge() != null
                && a.getAge() > r.getMaxAge()) {
            return false;
        }
        if (Boolean.TRUE.equals(r.getStudentOnly())
                && !Boolean.TRUE.equals(a.getIsStudent())) {
            return false;
        }
        if (a.getIncomeLevel() != null && r.getMaxIncomeLevel() != null
                && incomeRank(a.getIncomeLevel()) > incomeRank(r.getMaxIncomeLevel())) {
            return false;
        }
        return true;
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
            case "LOW":
                return 1;
            case "MEDIUM":
                return 2;
            default:
                return 3;
        }
    }

    private double location(Assessment a, Resource r) {
        if (a.getLocation() == null || r.getLocation() == null) {
            return 0.0;
        }

        String userLocation = a.getLocation().trim();
        String resourceLocation = r.getLocation().trim();

        if (userLocation.isBlank() || resourceLocation.isBlank()) {
            return 0.0;
        }

        return userLocation.equalsIgnoreCase(resourceLocation) ? 1.0 : 0.0;
    }

    private double availability(Resource r) {
        if (r.getAvailability() == null)
            return 0.5;
        switch (r.getAvailability().toUpperCase()) {
            case "OPEN":
                return 1.0;
            case "LIMITED":
                return 0.5;
            default:
                return 0.0;
        }
    }

    private double reliability(Resource r) {
        int baseScore = r.getReliabilityScore() == null ? 50 : r.getReliabilityScore();
        baseScore = Math.max(0, Math.min(100, baseScore));

        double freshnessScore = 1.0;

        if (r.getLastVerifiedDate() != null && !r.getLastVerifiedDate().isBlank()) {
            try {
                LocalDate verifiedDate = LocalDate.parse(r.getLastVerifiedDate());
                long daysOld = ChronoUnit.DAYS.between(verifiedDate, LocalDate.now());

                if (daysOld <= 30) {
                    freshnessScore = 1.0;
                } else if (daysOld <= 90) {
                    freshnessScore = 0.8;
                } else if (daysOld <= 180) {
                    freshnessScore = 0.6;
                } else {
                    freshnessScore = 0.4;
                }
            } catch (Exception e) {
                freshnessScore = 0.5;
            }
        }

        double verificationScore = "VERIFIED".equalsIgnoreCase(r.getVerificationStatus())
                ? 1.0
                : 0.7;

        double combinedScore = (baseScore / 100.0) * 0.6
                + freshnessScore * 0.25
                + verificationScore * 0.15;

        return Math.max(0.0, Math.min(1.0, combinedScore));
    }

    private double preference(Assessment a, Resource r) {
        if (a.getPreference() == null || a.getPreference().isBlank()
                || r.getServices() == null || r.getServices().isBlank()) {
            return 0.5;
        }

        String preference = a.getPreference().trim().toLowerCase();
        String services = r.getServices().toLowerCase();

        return services.contains(preference) ? 1.0 : 0.0;
    }
}