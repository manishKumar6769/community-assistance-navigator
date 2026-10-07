package com.navigator.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.navigator.model.Resource;
import com.navigator.service.ResourceService;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService service;

    public ResourceController(ResourceService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Resource resource) {
        if (resource.getName() == null || resource.getName().isBlank()
                || resource.getCategory() == null || resource.getCategory().isBlank()
                || resource.getLocation() == null || resource.getLocation().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "error", "Validation failed",
                    "message", "name, category and location are required"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(resource));
    }

    @GetMapping
    public List<Resource> list(@RequestParam(required = false) String category) {
        return service.list(category);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id) {
        Resource found = service.get(id);
        if (found == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", 404,
                    "error", "Not found",
                    "message", "No resource with id " + id));
        }
        return ResponseEntity.ok(found);
    }
}