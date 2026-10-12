import boto3  # pyright: ignore[reportMissingImports]
from datetime import datetime, timezone

table = boto3.resource("dynamodb", region_name="ap-south-1").Table("cana-Resources")

def score(days):
    if days <= 30: return 100
    if days <= 90: return 70
    if days <= 180: return 40
    return 10

def lambda_handler(event, context):
    now = datetime.now(timezone.utc)
    updated = 0
    kwargs = {}
    while True:
        resp = table.scan(**kwargs)
        for item in resp["Items"]:
            lv = item.get("lastVerified")
            if not lv:
                s = 0
            else:
                d = datetime.fromisoformat(lv.replace("Z", "+00:00"))
                if d.tzinfo is None:
                    d = d.replace(tzinfo=timezone.utc)
                s = score((now - d).days)
            table.update_item(
                Key={"resourceId": item["resourceId"]},
                UpdateExpression="SET freshnessScore = :s, freshnessCheckedAt = :t",
                ExpressionAttributeValues={":s": s, ":t": now.isoformat()},
            )
            updated += 1
        if "LastEvaluatedKey" not in resp:
            break
        kwargs["ExclusiveStartKey"] = resp["LastEvaluatedKey"]
    return {"updated": updated}