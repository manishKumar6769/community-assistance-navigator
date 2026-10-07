package com.navigator.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class Resource {

    private String resourceId;
    private String name;
    private String category;
    private String location;
    private String services;
    private Integer minAge;
    private Integer maxAge;
    private Boolean studentOnly;
    private String maxIncomeLevel;
    private String availability;
    private String contact;
    private String lastVerifiedDate;
    private String verificationStatus;
    private Integer reliabilityScore;

    public Resource() {}

    @DynamoDbPartitionKey
    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getServices() { return services; }
    public void setServices(String services) { this.services = services; }

    public Integer getMinAge() { return minAge; }
    public void setMinAge(Integer minAge) { this.minAge = minAge; }

    public Integer getMaxAge() { return maxAge; }
    public void setMaxAge(Integer maxAge) { this.maxAge = maxAge; }

    public Boolean getStudentOnly() { return studentOnly; }
    public void setStudentOnly(Boolean studentOnly) { this.studentOnly = studentOnly; }

    public String getMaxIncomeLevel() { return maxIncomeLevel; }
    public void setMaxIncomeLevel(String maxIncomeLevel) { this.maxIncomeLevel = maxIncomeLevel; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getLastVerifiedDate() { return lastVerifiedDate; }
    public void setLastVerifiedDate(String lastVerifiedDate) { this.lastVerifiedDate = lastVerifiedDate; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public Integer getReliabilityScore() { return reliabilityScore; }
    public void setReliabilityScore(Integer reliabilityScore) { this.reliabilityScore = reliabilityScore; }
}