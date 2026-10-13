#!/bin/bash
set -e
JAR_NAME=$1
BUCKET=cana-verification-docs-416964654377
APP=/opt/cana/navigator.jar

if [ -z "$JAR_NAME" ]; then
  echo "Usage: ./deploy.sh <jar-file-name-in-s3>"
  exit 1
fi

[ -f $APP ] && cp $APP $APP.bak

aws s3 cp s3://$BUCKET/$JAR_NAME $APP
sudo systemctl restart cana
sleep 30
curl -s -o /dev/null -w "Health check /api/resources: HTTP %{http_code}\n" http://localhost:8081/api/resources
echo "Deployed $JAR_NAME"