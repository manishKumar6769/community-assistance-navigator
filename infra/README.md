# AWS Infrastructure (Member 1)

Region: ap-south-1
AWS account: 416964654377
EC2 instance: i-0365bb6beaca8a28a (Assistant-Navigator), t3.micro, Amazon Linux 2023
Server address: http://13.233.40.108:8081 (Elastic IP)
Health check: /api/resources (HTTP 200)
App port: 8081
JAR: navigator-0.0.1-SNAPSHOT.jar (deployed as /opt/cana/navigator.jar)

## DynamoDB tables (on-demand, no prefix)
- Users (UserId)
- Resources (resourceId)
- Assessments (assessmentId)
- Referrals (referralId)
- Feedback (feedbackId)
- Documents (documentId)

## S3
- cana-verification-docs-416964654377 (private, versioned, encrypted)
- cana-cloudtrail-logs-416964654377 (CloudTrail logs)

## IAM
- cana-ec2-role: used by the EC2 instance (no access keys in code)
- cana-app-policy: DynamoDB access to the six tables + S3 bucket
- cana-lambda-role: used by the freshness Lambda
- cana-team: IAM group for teammates

## Lambda
- cana-freshness-check (daily, EventBridge rule cana-daily-freshness)
- Reads lastVerifiedDate, writes freshnessScore,
  fills reliabilityScore only when it is empty

## CloudTrail
- cana-audit-trail (management events)

## Security group (launch-wizard-4)
- 8081 from allowed IPs only. SSH (22) is closed. Access via Session Manager.

## Deploy
1. Build: ./mnt build in backend/: ./mvnw clean package -DskipTests
2. Upload target/navigator-0.0.1-SNAPSHOT.jar to the S3 bucket
3. On EC2 as ec2-user: /opt/cana/deploy.sh navigator-0.0.1-SNAPSHOT.jar
4. Rollback: cp /opt/cana/navigator.jar.bak /opt/cana/navigator.jar
             sudo systemctl restart cana

## Recovery
- EBS snapshot: Assistant-Navigator-working (taken 2026-10-13)