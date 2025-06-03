# 단일 스테이지, 빌드 없이 바로 실행
FROM eclipse-temurin:21-jre

WORKDIR /app

# curl 설치 (헬스체크용)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 미리 빌드된 JAR 파일 복사
COPY build/libs/app.jar app.jar

# 로그 디렉토리 생성
RUN mkdir -p /app/logs

# 환경 설정
ENV TZ=Asia/Seoul

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=40s \
  CMD curl -f http://localhost:8080/ping || exit 1

# 앱 실행
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
