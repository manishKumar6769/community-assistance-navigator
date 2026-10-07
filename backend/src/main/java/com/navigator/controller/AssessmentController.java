package com.navigator.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.navigator.model.Assessment;
import com.navigator.service.AssessmentService;

@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {

    private final AssessmentService service;

    public AssessmentController(AssessmentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Assessment assessment) {
        if (assessment.getNeedCategory() == null || assessment.getNeedCategory().isBlank()
                || assessment.getLocation() == null || assessment.getLocation().isBlank()
                || assessment.getAge() == null || assessment.getAge() <= 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "error", "Validation failed",
                    "message", "needCategory, location and a positive age are required"));
        }
        Assessment saved = service.create(assessment);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id) {
        Assessment found = service.get(id);
        if (found == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", 404,
                    "error", "Not found",
                    "message", "No assessment with id " + id));
        }
        return ResponseEntity.ok(found);
    }
}