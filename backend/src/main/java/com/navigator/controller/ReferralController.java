package com.navigator.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.navigator.model.Referral;
import com.navigator.service.ReferralService;

@RestController
@RequestMapping("/api/referrals")
public class ReferralController {

    private final ReferralService service;

    public ReferralController(ReferralService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Referral referral) {
        if (isBlank(referral.getUserId()) || isBlank(referral.getResourceId())) {
            return error(HttpStatus.BAD_REQUEST, "Validation failed",
                    "userId and resourceId are required");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(referral));
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam String userId) {
        return ResponseEntity.ok(service.listByUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id) {
        Referral found = service.get(id);
        if (found == null) {
            return error(HttpStatus.NOT_FOUND, "Not found", "No referral with id " + id);
        }
        return ResponseEntity.ok(found);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String id,
                                          @RequestBody Map<String, String> body) {
        try {
            Referral updated = service.updateStatus(id, body.get("status"), body.get("failureReason"));
            if (updated == null) {
                return error(HttpStatus.NOT_FOUND, "Not found", "No referral with id " + id);
            }
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "Validation failed", e.getMessage());
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private ResponseEntity<?> error(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "status", status.value(),
                "error", error,
                "message", message));
    }
}