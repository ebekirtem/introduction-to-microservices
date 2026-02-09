#!/bin/bash

# Wait for LocalStack to be ready
echo "Waiting for LocalStack to be ready..."
until curl -s http://localstack:4566/_localstack/health | grep -q '"s3": "available"'; do
  sleep 2
done

echo "LocalStack is ready. Creating S3 bucket..."

# Create S3 bucket
aws --endpoint-url=http://localstack:4566 s3 mb s3://resource-bucket --region us-east-1

echo "S3 bucket 'resource-bucket' created successfully!"
