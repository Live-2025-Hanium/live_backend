# Build stage
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copy gradle wrapper and build files
COPY gradle gradle
COPY gradlew .
COPY gradlew.bat .
COPY build.gradle .
COPY settings.gradle .

# Copy live-config submodule
COPY live-config live-config

# Copy source code
COPY src src

# Make gradlew executable
RUN chmod +x ./gradlew

# Build the application
RUN ./gradlew build -x test

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# Install curl for healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy built jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs

# Set timezone
ENV TZ=Asia/Seoul

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=40s \
  CMD curl -f http://localhost:8080/ping || exit 1

# Run the application
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"] 