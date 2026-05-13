#!/bin/sh
set -e

cat > /tmp/s3.conf << EOF
{
  "identities": [
    {
      "name": "admin",
      "credentials": [
        {
          "accessKey": "$STORAGE_ACCESS_KEY",
          "secretKey": "$STORAGE_SECRET_KEY"
        }
      ],
      "actions": ["Admin", "Read", "Write", "List", "Tagging"]
    }
  ]
}
EOF

exec weed server -dir=/data -s3 -s3.port=8333 -s3.config=/tmp/s3.conf
