package com.navigator.service;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.navigator.model.DocumentRecord;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class DocumentService {

    private final S3Client s3;
    private final DynamoDbTable<DocumentRecord> table;
    private final String bucket;

    public DocumentService(S3Client s3,
                           DynamoDbEnhancedClient enhancedClient,
                           @Value("${app.s3.bucket}") String bucket) {
        this.s3 = s3;
        this.bucket = bucket;
        this.table = enhancedClient.table("Documents", TableSchema.fromBean(DocumentRecord.class));
    }

    public DocumentRecord upload(String resourceId, MultipartFile file) throws IOException {
        String documentId = UUID.randomUUID().toString();
        String safeName = file.getOriginalFilename() == null
                ? "file" : file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
        String key = "resources/" + resourceId + "/" + documentId + "-" + safeName;

        s3.putObject(PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromBytes(file.getBytes()));

        DocumentRecord record = new DocumentRecord();
        record.setDocumentId(documentId);
        record.setResourceId(resourceId);
        record.setDocumentName(safeName);
        record.setS3Key(key);
        record.setVerificationStatus("PENDING");
        record.setUploadedAt(Instant.now().toString());
        table.putItem(record);
        return record;
    }
}
