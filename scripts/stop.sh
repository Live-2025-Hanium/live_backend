#!/bin/bash

echo "Stopping Live Backend application..."

# Docker Compose로 애플리케이션 중지
docker compose down

echo "🛑 Application stopped successfully!"

# 옵션: 이미지도 함께 제거하려면 아래 라인 주석 해제
# docker compose down --rmi all
