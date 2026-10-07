package com.navigator.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.navigator.model.Feedback;
import com.navigator.service.FeedbackService;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService service;

    public FeedbackController(FeedbackService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> submit(@RequestBody Feedback feedback) {
        if (feedback.getReferralId() == null || feedback.getReferralId().isBlank()
                || feedback.getRating() == null
                || feedback.getRating() < 1 || feedback.getRating() > 5) {
            return error(HttpStatus.BAD_REQUEST, "Validation failed",
                    "referralId and a rating from 1 to 5 are required");
        }
        Feedback saved = service.submit(feedback);
        if (saved == null) {
            return error(HttpStatus.NOT_FOUND, "Not found",
                    "No referral with id " + feedback.getReferralId());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    private ResponseEntity<?> error(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "status", status.value(),
                "error", error,
                "message", message));
    }
}