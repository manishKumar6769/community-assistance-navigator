package com.navigator.service;

import com.navigator.model.Referral;
import com.navigator.repository.ReferralRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final ReferralRepository referralRepository;

    public AnalyticsService(ReferralRepository referralRepository) {
        this.referralRepository = referralRepository;
    }

    public Map<String, Object> summary() {
        List<Referral> all = referralRepository.findAll();

        int requested = 0, contacted = 0, received = 0, notReceived = 0;
        Map<String, Integer> failureReasons = new LinkedHashMap<>();

        for (Referral r : all) {
            String status = r.getStatus() == null ? "" : r.getStatus();
            switch (status) {
                case "REQUESTED": requested++; break;
                case "CONTACTED": contacted++; break;
                case "RECEIVED": received++; break;
                case "NOT_RECEIVED":
                    notReceived++;
                    String reason = r.getFailureReason() == null ? "UNKNOWN" : r.getFailureReason();
                    failureReasons.merge(reason, 1, Integer::sum);
                    break;
                default: break;
            }
        }

        int completed = received + notReceived;
        double successRate = completed == 0 ? 0.0
                : Math.round(received * 1000.0 / completed) / 10.0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalReferrals", all.size());
        result.put("requested", requested);
        result.put("contacted", contacted);
        result.put("received", received);
        result.put("notReceived", notReceived);
        result.put("successRatePercent", successRate);
        result.put("failureReasons", failureReasons);
        return result;
    }
}
