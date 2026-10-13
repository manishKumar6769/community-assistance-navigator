import boto3
from datetime import datetime, timezone

dynamodb = boto3.resource("dynamodb", region_name="ap-south-1")
table = dynamodb.Table("Resources")

def score(days):
    if days <= 30: return 100
    if days <= 90: return 70
    if days <= 180: return 40
    return 10

def parse(v):
    d = datetime.fromisoformat(str(v).replace("Z", "+00:00"))
    return d if d.tzinfo else d.replace(tzinfo=timezone.utc)

def lambda_handler(event, context):
    now = datetime.now(timezone.utc)
    updated = 0
    kwargs = {}
    while True:
        resp = table.scan(**kwargs)
        for item in resp["Items"]:
            lv = item.get("lastVerifiedDate")
            try:
                s = score((now - parse(lv)).days) if lv else 0
            except Exception:
                s = 0
            table.update_item(
                Key={"resourceId": item["resourceId"]},
                UpdateExpression=(
                    "SET freshnessScore = :s, freshnessCheckedAt = :t, "
                    "reliabilityScore = if_not_exists(reliabilityScore, :s)"
                ),
                ExpressionAttributeValues={":s": s, ":t": now.isoformat()},
            )
            updated += 1
        if "LastEvaluatedKey" not in resp:
            break
        kwargs["ExclusiveStartKey"] = resp["LastEvaluatedKey"]
    return {"updated": updated}