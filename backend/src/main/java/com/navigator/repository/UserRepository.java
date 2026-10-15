package com.navigator.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.navigator.model.User;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

@Repository
public class UserRepository {

    private final DynamoDbTable<User> table;

    public UserRepository(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table(
            "Users",
            TableSchema.fromBean(User.class)
        );
    }

    public User save(User user) {
        table.putItem(user);
        return user;
    }

    public Optional<User> findById(String userId) {
        return Optional.ofNullable(
            table.getItem(
                Key.builder()
                    .partitionValue(userId)
                    .build()
            )
        );
    }

    public Optional<User> findByEmail(String email) {
        DynamoDbIndex<User> emailIndex =
            table.index("EmailIndex");

        QueryConditional condition =
            QueryConditional.keyEqualTo(
                Key.builder()
                    .partitionValue(email.trim().toLowerCase(
                        java.util.Locale.ROOT
                    ))
                    .build()
            );

        QueryEnhancedRequest request =
            QueryEnhancedRequest.builder()
                .queryConditional(condition)
                .limit(1)
                .build();

        return emailIndex.query(request)
            .stream()
            .flatMap(page -> page.items().stream())
            .findFirst();
    }

    public List<User> findAll() {
        return table.scan()
            .items()
            .stream()
            .toList();
    }
}
