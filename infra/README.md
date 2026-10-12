# AWS Infrastructure (Member 1)

Region: ap-south-1
EC2 instance: i-0365bb6beaca8a28a
Health check: /actuator/health
S3 bucket: cana-verification-docs-416964654377

DynamoDB tables:
- cana-Users (userId)
- cana-Resources (resourceId)
- cana-Assessments (assessmentId)
- cana-Referrals (referralId)
- cana-Feedback (feedbackId)
- cana-Verification (verificationId)

EC2 IAM role: cana-ec2-role (no access keys in code on EC2)
Lambda: cana-freshness-check (daily, writes freshnessScore)
CloudTrail: cana-audit-trail