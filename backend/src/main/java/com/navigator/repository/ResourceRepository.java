package com.navigator.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.navigator.model.Resource;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class ResourceRepository {

    private final DynamoDbTable<Resource> table;

    public ResourceRepository(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table("Resources", TableSchema.fromBean(Resource.class));
    }

    public Resource save(Resource resource) {
        table.putItem(resource);
        return resource;
    }

    public Resource findById(String resourceId) {
        return table.getItem(Key.builder().partitionValue(resourceId).build());
    }

    public List<Resource> findAll() {
        List<Resource> result = new ArrayList<>();
        table.scan().items().forEach(result::add);
        return result;
    }

    public List<Resource> findByCategory(String category) {
        List<Resource> result = new ArrayList<>();
        table.scan().items().forEach(r -> {
            if (category.equalsIgnoreCase(r.getCategory())) {
                result.add(r);
            }
        });
        return result;
    }
}