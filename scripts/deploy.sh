#!/bin/bash

# Deployment script for Docker Swarm service
# Usage: ./deploy.sh <image_name> <service_name>

set -e  # Exit on any error

# Check if required arguments are provided
if [ $# -ne 2 ]; then
    echo "Usage: $0 <image_name> <service_name>"
    echo "Example: $0 myapp:latest my-service"
    exit 1
fi

IMAGE_NAME="$1"
SERVICE_NAME="$2"

echo "Starting deployment..."
echo "Image: $IMAGE_NAME"
echo "Service: $SERVICE_NAME"

# Pull the new image
echo "Pulling new image..."
docker pull "$IMAGE_NAME"

# Update the service with rolling update
echo "Updating service..."
docker service update \
  --image "$IMAGE_NAME" \
  --update-parallelism 1 \
  --update-delay 10s \
  --update-failure-action rollback \
  --update-monitor 30s \
  --rollback-parallelism 1 \
  --rollback-delay 5s \
  --rollback-failure-action pause \
  --rollback-monitor 30s \
  "$SERVICE_NAME"

# Function to check service update status
check_service_status() {
  local service_name=$1
  local max_attempts=30
  local attempt=1

  while [ $attempt -le $max_attempts ]; do
    echo "Checking service status (attempt $attempt/$max_attempts)..."

    # Get service update status
    update_status=$(docker service ps "$service_name" --format "table {{.ID}}\t{{.Name}}\t{{.Image}}\t{{.CurrentState}}\t{{.Error}}" --no-trunc)
    echo "$update_status"

    # Check if update is complete
    running_count=$(docker service ps "$service_name" --filter "desired-state=running" --format "{{.CurrentState}}" | grep -c "Running")
    total_replicas=$(docker service inspect "$service_name" --format "{{.Spec.Replicas}}")

    if [ "$running_count" -eq "$total_replicas" ]; then
      echo "Service update completed successfully!"
      return 0
    fi

    # Check for failed tasks
    failed_count=$(docker service ps "$service_name" --filter "desired-state=running" --format "{{.CurrentState}}" | grep -c "Failed" || echo "0")
    if [ "$failed_count" -gt 0 ]; then
      echo "Service update failed!"
      return 1
    fi

    sleep 10
    attempt=$((attempt + 1))
  done

  echo "Service update timed out!"
  return 1
}

# Wait for the service to be updated
echo "Waiting for service update to complete..."
if check_service_status "$SERVICE_NAME"; then
  echo "Deployment successful!"

  # Clean up old images (keep last 3 versions)
  echo "Cleaning up old images..."
  BASE_IMAGE=$(echo "$IMAGE_NAME" | cut -d':' -f1)
  docker images "$BASE_IMAGE" --format "{{.Tag}}" | grep -v "latest" | sort -V | head -n -3 | xargs -r -I {} docker rmi "$BASE_IMAGE:{}" || true

  echo "Deployment completed successfully!"
else
  echo "Deployment failed!"
  exit 1
fi