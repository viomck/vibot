#!/bin/sh
# Builds vibot for production

./gradlew build
docker buildx build \
    --tag viomckinney/vibot:latest \
    -o type=image \
    --platform=linux/arm64,linux/amd64 \
    --push .
