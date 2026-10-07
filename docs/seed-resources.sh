#!/bin/bash
URL=http://localhost:8081/api/resources
post() {
  curl -s -X POST $URL -H "Content-Type: application/json" -d "$1" > /dev/null
  echo "added"
}

post '{"name":"Jamshedpur Scholarship Cell","category":"Education","location":"Jamshedpur","services":"Scholarship guidance","minAge":15,"maxAge":30,"studentOnly":true,"maxIncomeLevel":"MEDIUM","availability":"OPEN","contact":"0657-0000001","verificationStatus":"VERIFIED","reliabilityScore":85}'
post '{"name":"Bright Future Trust","category":"Education","location":"Ranchi","services":"Free coaching","minAge":12,"maxAge":25,"studentOnly":true,"maxIncomeLevel":"LOW","availability":"LIMITED","contact":"0651-0000002","verificationStatus":"VERIFIED","reliabilityScore":78}'
post '{"name":"Learn Together NGO","category":"Education","location":"Jamshedpur","services":"Books and fee support","minAge":6,"maxAge":22,"studentOnly":true,"maxIncomeLevel":"LOW","availability":"OPEN","contact":"0657-0000003","verificationStatus":"UNVERIFIED","reliabilityScore":60}'
post '{"name":"City Health Camp","category":"Health","location":"Jamshedpur","services":"Free checkups","minAge":0,"maxAge":99,"studentOnly":false,"maxIncomeLevel":"HIGH","availability":"OPEN","contact":"0657-0000004","verificationStatus":"VERIFIED","reliabilityScore":90}'
post '{"name":"Care Clinic Trust","category":"Health","location":"Dhanbad","services":"Low-cost treatment","minAge":0,"maxAge":99,"studentOnly":false,"maxIncomeLevel":"LOW","availability":"LIMITED","contact":"0326-0000005","verificationStatus":"VERIFIED","reliabilityScore":72}'
post '{"name":"Anna Daan Kitchen","category":"Food","location":"Jamshedpur","services":"Free meals","minAge":0,"maxAge":99,"studentOnly":false,"maxIncomeLevel":"LOW","availability":"OPEN","contact":"0657-0000006","verificationStatus":"VERIFIED","reliabilityScore":88}'
post '{"name":"Ranchi Food Bank","category":"Food","location":"Ranchi","services":"Monthly ration","minAge":18,"maxAge":99,"studentOnly":false,"maxIncomeLevel":"LOW","availability":"OPEN","contact":"0651-0000007","verificationStatus":"UNVERIFIED","reliabilityScore":55}'
post '{"name":"Free Legal Aid Centre","category":"Legal","location":"Jamshedpur","services":"Legal advice","minAge":18,"maxAge":99,"studentOnly":false,"maxIncomeLevel":"MEDIUM","availability":"OPEN","contact":"0657-0000008","verificationStatus":"VERIFIED","reliabilityScore":80}'
post '{"name":"Skill Up Employment Hub","category":"Employment","location":"Jamshedpur","services":"Job training","minAge":18,"maxAge":40,"studentOnly":false,"maxIncomeLevel":"MEDIUM","availability":"OPEN","contact":"0657-0000009","verificationStatus":"VERIFIED","reliabilityScore":83}'
post '{"name":"Youth Career Point","category":"Employment","location":"Ranchi","services":"Resume and interview help","minAge":18,"maxAge":30,"studentOnly":false,"maxIncomeLevel":"HIGH","availability":"CLOSED","contact":"0651-0000010","verificationStatus":"UNVERIFIED","reliabilityScore":40}'