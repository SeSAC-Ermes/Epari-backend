# Build stage
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# 빌드 캐시 최적화를 위해 의존성 파일 먼저 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# gradlew 실행 권한 부여 및 의존성 다운로드
RUN chmod +x gradlew
RUN ./gradlew dependencies

# 소스 복사 및 빌드
COPY src src
RUN ./gradlew build -x test

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 타임존 설정
ENV TZ=Asia/Seoul

# 업로드 디렉토리 생성
RUN mkdir -p /app/uploads

# 빌드된 JAR 파일만 복사 (Gradle의 경우 build/libs 디렉토리에 생성됨)
COPY --from=builder /app/build/libs/*.jar app.jar

# JVM 튜닝
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# 8080 포트 노출
EXPOSE 8080

# 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
