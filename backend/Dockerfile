# 1단계: 빌드 스테이지
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# Gradle Wrapper & Build Config 먼저 복사 (의존성 캐시 최적화)
COPY gradlew gradlew.bat settings.gradle* build.gradle* ./
COPY gradle/ ./gradle/

# Gradle Wrapper 실행 권한 부여
RUN chmod +x ./gradlew

# 의존성 다운로드 (변경 없으면 캐시 활용)
RUN ./gradlew dependencies --no-daemon

# 전체 소스 복사 후 빌드
COPY . .
RUN ./gradlew bootJar --no-daemon

# 2단계: 실행 스테이지
FROM openjdk:17-jdk-slim
WORKDIR /app

# 빌드 결과 JAR 파일 복사 (필요시 명시적 파일명으로 변경)
COPY --from=builder /app/build/libs/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
