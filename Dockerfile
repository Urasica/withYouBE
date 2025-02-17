# Gradle Wrapper를 사용하여 빌드하기 위한 도커 이미지
FROM openjdk:17-jdk-slim AS build

# 작업 디렉토리 설정
WORKDIR /app

# 프로젝트 파일을 컨테이너로 복사
COPY . .
RUN chmod +x ./gradlew
# Gradle Wrapper를 사용하여 빌드
RUN ./gradlew clean build --no-daemon -x test

# 2. 최종 실행 이미지 (JDK 17)
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# 환경변수 설정 (docker-compose 환경에서만 redis 호스트를 설정)
ARG REDIS_HOST=redis
ENV REDIS_HOST=$REDIS_HOST

# Gradle 빌드된 JAR 파일을 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 포트 열기
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]