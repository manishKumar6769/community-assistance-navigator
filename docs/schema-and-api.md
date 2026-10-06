# Schema and API Contract (v1)

Status: FROZEN on Day 2. Any change must be announced to the whole team.
Region: ap-south-1. All IDs are strings (UUIDs). Dates use ISO format (2026-10-07).

## 1. DynamoDB tables

### Users
- Partition key: userId
- Fields: name, email, role (USER or ADMIN), createdAt

### Resources
- Partition key: resourceId
- Fields: name, category (Education, Health, Food, Legal, Employment), location, services,
  eligibility (minAge, maxAge, studentOnly, maxIncomeLevel), availability (OPEN or LIMITED or CLOSED),
  contact, lastVerifiedDate, verificationStatus, reliabilityScore (0 to 100)
- Index: category-index (partition key: category)

### Assessments
- Partition key: assessmentId
- Fields: userId, needCategory, location, age, isStudent, incomeLevel (LOW, MEDIUM, HIGH),
  preference, createdAt
- Index: userId-index (partition key: userId)

### Referrals
- Partition key: referralId
- Fields: userId, resourceId, assessmentId,
  status (REQUESTED, CONTACTED, RECEIVED, NOT_RECEIVED),
  failureReason (NO_RESPONSE, NOT_ELIGIBLE, TOO_FAR, UNAVAILABLE, INCORRECT_INFO),
  createdAt, updatedAt
- Indexes: userId-index (partition key: userId), resourceId-index (partition key: resourceId)

### Feedback
- Partition key: feedbackId
- Fields: referralId, resourceId, rating (1 to 5), comment, createdAt
- Index: resourceId-index (partition key: resourceId)

### Document metadata (stored in Resources or a separate item)
- Fields: resourceId, documentName, s3Key, verificationStatus, uploadedAt
- The file itself is stored in S3, never in DynamoDB.

## 2. REST APIs

| Method | Endpoint | Purpose | Main input | Main output |
|---|---|---|---|---|
| POST | /api/assessments | Save a need assessment | assessment fields | assessmentId |
| GET | /api/recommendations?assessmentId= | Ranked resources | assessmentId | list of resources with matchScore |
| GET | /api/resources/{id} | Resource details | resourceId | resource |
| POST | /api/referrals | Request assistance | userId, resourceId, assessmentId | referral |
| GET | /api/referrals?userId= | Track my requests | userId | list of referrals |
| PUT | /api/referrals/{id}/status | Update status or failure reason | status, failureReason | referral |
| POST | /api/feedback | Submit outcome feedback | referralId, rating, comment | feedback |
| POST | /api/resources/{id}/documents | Upload a document to S3 | file | s3Key, status |
| GET | /api/admin/analytics | Admin statistics | none | totals, success rate, top failure reasons |

## 3. Error format (all APIs)

```json
{ "status": 400, "error": "Validation failed", "message": "age must be positive" }
```

## 4. Ownership

- Member 2: tables, repositories, controllers, services
- Member 4: matching and reliability logic in package com.navigator.matching
- Member 3: calls the APIs above
- Member 5: tests and admin analytics