package com.navigator.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.navigator.model.DocumentRecord;
import com.navigator.service.DocumentService;
import com.navigator.service.ResourceService;

@RestController
@RequestMapping("/api/resources")
public class DocumentController {

    private final DocumentService documentService;
    private final ResourceService resourceService;

    public DocumentController(DocumentService documentService, ResourceService resourceService) {
        this.documentService = documentService;
        this.resourceService = resourceService;
    }

    @PostMapping("/{id}/documents")
    public ResponseEntity<?> upload(@PathVariable String id,
                                    @RequestParam("file") MultipartFile file) {
        if (resourceService.get(id) == null) {
            return error(HttpStatus.NOT_FOUND, "Not found", "No resource with id " + id);
        }
        if (file.isEmpty()) {
            return error(HttpStatus.BAD_REQUEST, "Validation failed", "file is empty");
        }
        try {
            DocumentRecord record = documentService.upload(id, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(record);
        } catch (Exception e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed", e.getMessage());
        }
    }

    private ResponseEntity<?> error(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "status", status.value(), "error", error, "message", String.valueOf(message)));
    }
}