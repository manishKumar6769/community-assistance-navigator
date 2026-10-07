package com.navigator.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class Assessment {

    private String assessmentId;
    private String userId;
    private String needCategory;
    private String location;
    private Integer age;
    private Boolean isStudent;
    private String incomeLevel;
    private String preference;
    private String createdAt;

    public Assessment() {}

    @DynamoDbPartitionKey
    public String getAssessmentId() { return assessmentId; }
    public void setAssessmentId(String assessmentId) { this.assessmentId = assessmentId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getNeedCategory() { return needCategory; }
    public void setNeedCategory(String needCategory) { this.needCategory = needCategory; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Boolean getIsStudent() { return isStudent; }
    public void setIsStudent(Boolean isStudent) { this.isStudent = isStudent; }

    public String getIncomeLevel() { return incomeLevel; }
    public void setIncomeLevel(String incomeLevel) { this.incomeLevel = incomeLevel; }

    public String getPreference() { return preference; }
    public void setPreference(String preference) { this.preference = preference; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}