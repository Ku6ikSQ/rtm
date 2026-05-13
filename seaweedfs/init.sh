#!/bin/sh
set -e

apk add --no-cache curl aws-cli >/dev/null 2>&1

echo "Writing IAM config to filer..."
cat > /tmp/iam.json << EOF
{
  "signingKey": "seaweedfs-sts-key",
  "identities": [
    {
      "name": "admin",
      "credentials": [
        {
          "accessKey": "$AWS_ACCESS_KEY_ID",
          "secretKey": "$AWS_SECRET_ACCESS_KEY"
        }
      ],
      "actions": ["Admin", "Read", "Write", "List", "Tagging"]
    }
  ]
}
EOF

curl -sf -X POST -F "file=@/tmp/iam.json" "http://seaweedfs:8888/etc/iam/config.json"
echo "IAM config written, waiting for S3 to reload..."
sleep 5

echo "Creating bucket..."
aws --endpoint-url "http://seaweedfs:8333" s3api create-bucket \
  --bucket "$STORAGE_BUCKET" 2>&1 || true

echo "SeaweedFS init complete"
