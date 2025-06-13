#!/bin/bash

echo "Live Backend 애플리케이션 시작 중..."

# 작업 디렉토리 이동
cd /home/ec2-user/app || exit 1

# 미리 빌드된 이미지 tar 파일이 있으면 로드
if [ -f live-backend.tar ]; then
  echo "🛠 tar 파일에서 Docker 이미지 로드 중..."
  docker load < live-backend.tar
else
  echo "⚠️ Docker 이미지 tar 파일 (live-backend.tar)을 찾을 수 없습니다!"
  exit 1
fi

# Docker Compose로 애플리케이션 시작 (build 생략)
docker compose up -d

# 애플리케이션이 정상적으로 시작될 때까지 대기
echo "애플리케이션 시작 대기 중..."
for i in {1..30}; do
    if curl -f http://localhost:8080/ping >/dev/null 2>&1; then
        echo "✅ 애플리케이션이 8080 포트에서 성공적으로 시작되었습니다"
        echo "🌐 애플리케이션 접속 URL: http://localhost:8080"
        echo "📋 Swagger UI: http://localhost:8080/swagger-ui.html"
        break
    fi
    echo "대기 중... ($i/30)"
    sleep 3
done

if ! curl -f http://localhost:8080/ping >/dev/null 2>&1; then
    echo "❌ 90초 내에 애플리케이션 시작에 실패했습니다"
    echo "로그 확인: docker compose logs -f"
    exit 1
fi

echo "🚀 애플리케이션 배포가 성공적으로 완료되었습니다!"
