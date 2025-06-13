#!/bin/bash

# 작업 디렉토리를 /home/ec2-user/app으로 변경
cd /home/ec2-user/app

# 환경변수 DOCKER_APP_NAME을 live-backend로 설정
DOCKER_APP_NAME=live-backend

# 실행중인 blue가 있는지 확인
# 프로젝트의 실행 중인 컨테이너를 확인하고, 해당 컨테이너가 실행 중인지 여부를 EXIST_BLUE 변수에 저장
EXIST_BLUE=$(docker-compose -p ${DOCKER_APP_NAME}-blue -f docker-compose.blue.yml ps | grep Up)

# 배포 시작한 날짜와 시간을 기록
echo "배포 시작일자 : $(date +%Y)-$(date +%m)-$(date +%d) $(date +%H):$(date +%M):$(date +%S)" >> /home/ec2-user/deploy.log

# green이 실행중이면 blue up
# EXIST_BLUE 변수가 비어있는지 확인
if [ -z "$EXIST_BLUE" ]; then

  # 로그 파일(/home/ec2-user/deploy.log)에 "blue 배포 시작"이라는 내용을 추가
  echo "blue 배포 시작 : $(date +%Y)-$(date +%m)-$(date +%d) $(date +%H):$(date +%M):$(date +%S)" >> /home/ec2-user/deploy.log

  # Docker 이미지 로드
  if [ -f live-backend.tar ]; then
    echo "🛠 Docker 이미지 로드 중..."
    docker load < live-backend.tar
  fi

  # docker-compose.blue.yml 파일을 사용하여 live-backend-blue 프로젝트의 컨테이너를 빌드하고 실행
  docker-compose -p ${DOCKER_APP_NAME}-blue -f docker-compose.blue.yml up -d

  # 30초 동안 대기
  sleep 30
  
  # blue가 문제 없이 배포 되었는지 현재 실행여부를 확인한다
  BLUE_HEALTH=$(docker-compose -p ${DOCKER_APP_NAME}-blue -f docker-compose.blue.yml ps | grep Up)

  # blue가 현재 실행중이지 않다면 -> 런타임 에러 또는 다른 이유로 배포가 되지 못한 상태
  if [ -z "$BLUE_HEALTH" ]; then
    echo "❌ blue 배포 실패 : $(date +%Y)-$(date +%m)-$(date +%d) $(date +%H):$(date +%M):$(date +%S)" >> /home/ec2-user/deploy.log
    # Slack 알람을 보낼 수 있는 스크립트를 실행한다.
    # ./slack_blue.sh
  # blue가 현재 실행되고 있는 경우에만 green을 종료
  else
    # nginx가 자동으로 로드밸런싱하므로 수동 전환 불필요
    echo "✅ blue 배포 성공, nginx가 자동으로 트래픽을 분산합니다" >> /home/ec2-user/deploy.log
    
    # /home/ec2-user/deploy.log: 로그 파일에 "green 중단 시작"이라는 내용을 추가
    echo "green 중단 시작 : $(date +%Y)-$(date +%m)-$(date +%d) $(date +%H):$(date +%M):$(date +%S)" >> /home/ec2-user/deploy.log

    # docker-compose.green.yml 파일을 사용하여 live-backend-green 프로젝트의 컨테이너를 중지
    docker-compose -p ${DOCKER_APP_NAME}-green -f docker-compose.green.yml down

    # 사용하지 않는 이미지 삭제
    docker image prune -af

    echo "green 중단 완료 : $(date +%Y)-$(date +%m)-$(date +%d) $(date +%H):$(date +%M):$(date +%S)" >> /home/ec2-user/deploy.log
  fi

# blue가 실행중이면 green up
else
  echo "green 배포 시작 : $(date +%Y)-$(date +%m)-$(date +%d) $(date +%H):$(date +%M):$(date +%S)" >> /home/ec2-user/deploy.log
  
  # Docker 이미지 로드
  if [ -f live-backend.tar ]; then
    echo "🛠 Docker 이미지 로드 중..."
    docker load < live-backend.tar
  fi

  docker-compose -p ${DOCKER_APP_NAME}-green -f docker-compose.green.yml up -d

  sleep 30

  GREEN_HEALTH=$(docker-compose -p ${DOCKER_APP_NAME}-green -f docker-compose.green.yml ps | grep Up)

  if [ -z "$GREEN_HEALTH" ]; then
    echo "❌ green 배포 실패 : $(date +%Y)-$(date +%m)-$(date +%d) $(date +%H):$(date +%M):$(date +%S)" >> /home/ec2-user/deploy.log
    # ./slack_green.sh
  else
    # nginx가 자동으로 로드밸런싱하므로 수동 전환 불필요
    echo "✅ green 배포 성공, nginx가 자동으로 트래픽을 분산합니다" >> /home/ec2-user/deploy.log
    
    # /home/ec2-user/deploy.log: 로그 파일에 "blue 중단 시작"이라는 내용을 추가
    echo "blue 중단 시작 : $(date +%Y)-$(date +%m)-$(date +%d) $(date +%H):$(date +%M):$(date +%S)" >> /home/ec2-user/deploy.log

    # docker-compose.blue.yml 파일을 사용하여 live-backend-blue 프로젝트의 컨테이너를 중지
    docker-compose -p ${DOCKER_APP_NAME}-blue -f docker-compose.blue.yml down

    # 사용하지 않는 이미지 삭제
    docker image prune -af

    echo "blue 중단 완료 : $(date +%Y)-$(date +%m)-$(date +%d) $(date +%H):$(date +%M):$(date +%S)" >> /home/ec2-user/deploy.log
  fi
fi

echo "배포 종료  : $(date +%Y)-$(date +%m)-$(date +%d) $(date +%H):$(date +%M):$(date +%S)" >> /home/ec2-user/deploy.log
echo "===================== 배포 완료 =====================" >> /home/ec2-user/deploy.log
echo >> /home/ec2-user/deploy.log 