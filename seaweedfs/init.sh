#!/bin/sh
set -e

echo "Waiting for SeaweedFS filer..."
until curl -s --connect-timeout 2 -o /dev/null "http://seaweedfs:8888/" 2>&1; do
  sleep 3
done
echo "Filer is ready, waiting for S3..."
until curl -s --connect-timeout 2 -o /dev/null "http://seaweedfs:8333/" 2>&1; do
  sleep 2
done
echo "S3 is ready"

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
echo "IAM config written to filer"

sleep 5

aws s3api create-bucket \
  --bucket "$STORAGE_BUCKET" \
  --endpoint-url "http://seaweedfs:8333" || true

echo "SeaweedFS init complete"
