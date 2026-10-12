#!/bin/bash
set -e
JAR_NAME=$1
BUCKET=cana-verification-docs-416964654377

if [ -z "$JAR_NAME" ]; then
  echo "Usage: ./deploy.sh <jar-file-name-in-s3>"
  exit 1
fi

[ -f /opt/cana/hello.jar ] && cp /opt/cana/hello.jar /opt/cana/hello.jar.bak

aws s3 cp s3://$BUCKET/$JAR_NAME /opt/cana/hello.jar
sudo systemctl restart cana
sleep 15
curl -s http://localhost:8080/actuator/health && echo
echo "Deployed $JAR_NAME"