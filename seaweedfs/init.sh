#!/bin/sh
set -e

apk add --no-cache aws-cli >/dev/null 2>&1

echo "Waiting for SeaweedFS S3..."
until aws --endpoint-url "http://seaweedfs:8333" s3api list-buckets \
    --output text >/dev/null 2>&1; do
  sleep 3
done

echo "Creating bucket..."
aws --endpoint-url "http://seaweedfs:8333" s3api create-bucket \
  --bucket "$STORAGE_BUCKET" 2>&1 || true

echo "SeaweedFS init complete"
