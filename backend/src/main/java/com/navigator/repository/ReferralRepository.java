package com.navigator.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.navigator.model.Referral;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class ReferralRepository {

    private final DynamoDbTable<Referral> table;

    public ReferralRepository(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table("Referrals", TableSchema.fromBean(Referral.class));
    }

    public Referral save(Referral referral) {
        table.putItem(referral);
        return referral;
    }

    public Referral findById(String referralId) {
        return table.getItem(Key.builder().partitionValue(referralId).build());
    }

    public List<Referral> findAll() {
        List<Referral> result = new ArrayList<>();
        table.scan().items().forEach(result::add);
        return result;
    }

    public List<Referral> findByUserId(String userId) {
        List<Referral> result = new ArrayList<>();
        table.scan().items().forEach(r -> {
            if (userId.equals(r.getUserId())) {
                result.add(r);
            }
        });
        return result;
    }
}