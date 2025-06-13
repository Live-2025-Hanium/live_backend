#!/bin/bash

echo "Live Backend 애플리케이션 중지 중..."

# 작업 디렉토리 이동
cd /home/ec2-user/app || exit 1

# Docker Compose로 애플리케이션 중지
docker-compose down

echo "🛑 애플리케이션이 성공적으로 중지되었습니다!"

# 옵션: 이미지도 함께 제거하려면 아래 라인 주석 해제
# docker-compose down --rmi all
