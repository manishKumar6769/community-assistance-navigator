# Matching & Assistance Engine — Developer Guide

## 1. Overview

The Matching & Assistance Engine is responsible for finding and ranking community resources based on a user's assessment.

The engine evaluates resources using five weighted factors:

| Matching factor |   Weight |
| --------------- | -------: |
| Eligibility     |      30% |
| Location        |      25% |
| Availability    |      20% |
| Reliability     |      15% |
| User preference |      10% |
| **Total**       | **100%** |

The backend is built with Java and Spring Boot. Resource data is stored in AWS DynamoDB.

## 2. Relevant Source Files

The main files for this feature are:

```text
backend/
└── src/
    ├── main/java/com/navigator/
    │   ├── matching/
    │   │   └── MatchingEngine.java
    │   ├── service/
    │   │   ├── RecommendationService.java
    │   │   ├── ResourceService.java
    │   │   ├── ReferralService.java
    │   │   └── FeedbackService.java
    │   ├── controller/
    │   │   ├── RecommendationController.java
    │   │   └── ResourceController.java
    │   ├── model/
    │   │   ├── Assessment.java
    │   │   └── Resource.java
    │   └── repository/
    │       └── ResourceRepository.java
    └── test/java/com/navigator/
        ├── MatchingEngineTest.java
        ├── ReferralServiceTest.java
        ├── ReferralControllerTest.java
        └── FeedbackServiceTest.java
```

## 3. How Matching Works

### Step 1: User submits an assessment

The assessment contains information such as:

- Need category
- Location
- Age
- Student status
- Income level
- Preferred service

The assessment is handled by the backend assessment flow.

### Step 2: Resources are retrieved

`RecommendationService.java` retrieves resources whose category matches the assessment's `needCategory`.

Resource data is accessed through `ResourceRepository.java`, which uses the DynamoDB `Resources` table.

### Step 3: Eligibility is checked

Before ranking, `RecommendationService` calls:

```java
engine.isEligible(assessment, resource)
```

Resources that fail the eligibility checks are excluded from recommendations.

Current eligibility checks include age limits, student-only restrictions and maximum income level.

### Step 4: Matching scores are calculated

`MatchingEngine.java` calculates a score from 0 to 100 using the five weighted factors.

### Step 5: Recommendations are ranked

Eligible resources are sorted by match score in descending order, so higher-scoring resources appear first.

## 4. How to Improve the Matching Algorithm

All matching-score changes should start in:

```text
backend/src/main/java/com/navigator/matching/MatchingEngine.java
```

### Example: Improve location matching

The current implementation gives a full location score when the user's location and resource location match exactly, ignoring letter case.

A future improvement could support nearby locations or configurable location groups.

Before changing the algorithm:

1. Define the intended matching behaviour.
2. Update the relevant scoring method.
3. Add tests for matching and non-matching cases.
4. Run the complete backend test suite.
5. Verify recommendations through the API.

### Important rules

- Keep the five weights consistent with the intended scoring model.
- If weights change, ensure their total remains 100%.
- Do not recommend resources that fail mandatory eligibility checks.
- Handle missing or invalid resource data safely.
- Avoid making assumptions about a user's eligibility when required information is missing.
- Add regression tests for every changed behaviour.

## 5. How to Add a New Assistance Category

For example, to add Financial Assistance:

### Step 1: Add or verify resource data

Resources are stored in the DynamoDB `Resources` table.

The backend exposes:

```http
GET  /api/resources
POST /api/resources
GET  /api/resources/{id}
```

A resource must include at least `name`, `category` and `location` when created.

Example request body:

```json
{
  "name": "Example Financial Support Centre",
  "category": "Financial Assistance",
  "location": "Jamshedpur",
  "services": "Emergency financial aid",
  "minAge": 18,
  "maxAge": 99,
  "studentOnly": false,
  "maxIncomeLevel": "LOW",
  "availability": "OPEN",
  "contact": "CONTACT_TO_BE_VERIFIED",
  "reliabilityScore": 50,
  "verificationStatus": "UNVERIFIED"
}
```

Use genuine, verified provider details before treating a resource as a real service. Example entries are for development and testing only.

### Step 2: Update frontend category mapping

The frontend is located in:

```text
frontend/src/App.jsx
```

Check the category list and the mapping between frontend labels and backend category values.

The category submitted in an assessment must match the resource category stored in DynamoDB. Category names should be consistent across both sides.

### Step 3: Test recommendations

Verify that:

- Resources load for the new category.
- An assessment can be submitted for that category.
- The recommendation endpoint returns matching resources.
- Ineligible resources are excluded.
- A user can create an assistance request for a returned resource.

No new field in `Resource.java` is required if the existing `category` field is sufficient.

## 6. How to Run the Project

### Start the backend

Open a terminal at the project root:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The frontend expects the backend API at:

```text
http://localhost:8081
```

AWS credentials and DynamoDB configuration must be available for operations that access AWS resources.

### Start the frontend

Open a separate terminal:

```powershell
cd frontend
npm ci
npm run dev
```

Open the local URL printed by Vite, usually:

```text
http://localhost:5173
```

## 7. How to Run Tests

### Backend tests

From the project root:

```powershell
cd backend
.\mvnw.cmd test
```

Tests cover matching logic, feedback, referral behaviour and other backend functionality.

### Frontend lint

From the project root:

```powershell
cd frontend
npm run lint
```

### Frontend production build

```powershell
npm run build
```

All tests, lint checks and builds should pass before submitting changes.

## 8. Important API Endpoints

| Endpoint                                     | Purpose                         |
| -------------------------------------------- | ------------------------------- |
| `GET /api/resources`                         | Retrieve resources              |
| `GET /api/resources?category=Education`      | Filter resources by category    |
| `POST /api/assessments`                      | Submit an assessment            |
| `GET /api/recommendations?assessmentId={id}` | Retrieve ranked recommendations |
| `POST /api/referrals`                        | Create an assistance request    |
| `GET /api/referrals?userId={id}`             | Retrieve a user's requests      |
| `PUT /api/referrals/{id}/status`             | Update request status           |
| `POST /api/feedback`                         | Submit feedback                 |

Check the controller classes for exact request bodies and validation rules before integrating new clients.

## 9. Git Workflow for Contributors

Work on the agreed feature branch. For this team workflow, the branch is:

```text
feature/matching
```

Before making changes:

```powershell
git status
git pull origin feature/matching
```

Only pull when the working tree is clean and you have coordinated with teammates working on the same branch.

After implementing changes:

```powershell
git diff
git status
```

Run the relevant tests, lint and build. Stage only the files related to your work.

```powershell
git add <specific-files>
git commit -m "Describe the matching improvement"
git push origin feature/matching
```

Avoid force-pushing or committing unrelated files. Coordinate with teammates before pushing if multiple contributors are sharing the same branch.

## 10. Before Submitting a Change

Use this checklist:

- [ ] Matching behaviour is documented.
- [ ] Mandatory eligibility rules still work.
- [ ] Match scores and ranking are correct.
- [ ] New categories use consistent names across frontend and backend.
- [ ] Resource data is valid and appropriately verified.
- [ ] Tests cover normal cases and edge cases.
- [ ] Backend tests pass.
- [ ] Frontend lint and build pass when frontend files change.
- [ ] Only relevant files are committed.
- [ ] Changes are pushed to the agreed branch.

## 11. Current Limitations and Future Improvements

Potential improvements include:

- More flexible geographic matching.
- More robust handling of incomplete assessment data.
- Better preference matching than simple text containment.
- Expanded tests for score boundaries and eligibility edge cases.
- More detailed recommendation explanations for users.
- Improved validation and maintenance of resource verification data.

Any future change should preserve the existing referral flow and be validated with automated tests before merging.

---

**Contribution principle:** Understand the existing implementation, make the smallest correct change, add tests for the new behaviour, and verify the full affected flow before pushing.
