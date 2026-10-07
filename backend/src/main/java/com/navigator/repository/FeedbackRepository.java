package com.navigator.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.navigator.model.Feedback;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class FeedbackRepository {

    private final DynamoDbTable<Feedback> table;

    public FeedbackRepository(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table("Feedback", TableSchema.fromBean(Feedback.class));
    }

    public Feedback save(Feedback feedback) {
        table.putItem(feedback);
        return feedback;
    }

    public List<Feedback> findAll() {
        List<Feedback> result = new ArrayList<>();
        table.scan().items().forEach(result::add);
        return result;
    }
}