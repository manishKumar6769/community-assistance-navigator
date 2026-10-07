package com.navigator.repository;

import org.springframework.stereotype.Repository;

import com.navigator.model.Assessment;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class AssessmentRepository {

    private final DynamoDbTable<Assessment> table;

    public AssessmentRepository(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table("Assessments", TableSchema.fromBean(Assessment.class));
    }

    public Assessment save(Assessment assessment) {
        table.putItem(assessment);
        return assessment;
    }

    public Assessment findById(String assessmentId) {
        return table.getItem(Key.builder().partitionValue(assessmentId).build());
    }
}