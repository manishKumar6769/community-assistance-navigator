package com.navigator.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.navigator.service.AnalyticsService;

@RestController
@RequestMapping("/api/admin")
public class AnalyticsController {

    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/analytics")
    public Map<String, Object> analytics() {
        return service.summary();
    }
}
