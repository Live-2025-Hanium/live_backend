#!/bin/bash

echo "Starting Live Backend application..."

# 작업 디렉토리 이동
cd /home/ubuntu/app || exit 1

# 미리 빌드된 이미지 tar 파일이 있으면 로드
if [ -f live-backend.tar ]; then
  echo "🛠 Loading Docker image from tar..."
  docker load < live-backend.tar
else
  echo "⚠️ Docker image tar (live-backend.tar) not found!"
  exit 1
fi

# Docker Compose로 애플리케이션 시작 (build생략)
docker compose up -d

# 애플리케이션이 정상적으로 시작될 때까지 대기
echo "Waiting for application to start..."
for i in {1..30}; do
    if curl -f http://localhost:8080/ping >/dev/null 2>&1; then
        echo "✅ Application started successfully on port 8080"
        echo "🌐 Application is accessible at: http://localhost:8080"
        echo "📋 Swagger UI: http://localhost:8080/swagger-ui.html"
        break
    fi
    echo "Waiting... ($i/30)"
    sleep 3
done

if ! curl -f http://localhost:8080/ping >/dev/null 2>&1; then
    echo "❌ Application failed to start within 90 seconds"
    echo "Check logs with: docker compose logs -f"
    exit 1
fi

echo "🚀 Application deployment completed successfully!"
