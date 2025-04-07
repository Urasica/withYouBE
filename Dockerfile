# Gradle Wrapper를 사용하여 빌드하기 위한 도커 이미지
FROM openjdk:17-jdk-slim AS build

# 작업 디렉토리 설정
WORKDIR /app

# 프로젝트 파일을 컨테이너로 복사
COPY . .

# 시간대 설정 (빌드 시 적용)
RUN ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && echo "Asia/Seoul" > /etc/timezone

# 실행 권한 부여
RUN chmod +x ./gradlew

# Gradle Wrapper를 사용하여 빌드
RUN ./gradlew clean build --no-daemon -x test

# 최종 실행 이미지 (JDK 17)
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# Python 설치
RUN apt-get update && apt-get install -y python3 python3-pip && ln -s /usr/bin/python3 /usr/bin/python

# finance-datareader 설치
RUN pip3 install -U finance-datareader
RUN pip3 install -U numpy

# 환경변수 설정 (docker-compose 환경에서만 redis 호스트를 설정)
ARG REDIS_HOST=redis
ENV REDIS_HOST=$REDIS_HOST

# 시간대 설정 (실행 시 적용)
ENV TZ=Asia/Seoul
RUN ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && echo "Asia/Seoul" > /etc/timezone

# Gradle 빌드된 JAR 파일을 복사
COPY --from=build /app/build/libs/*.jar app.jar

# Python 코드 복사
COPY --from=build /app/src/main/java/com/capstone/withyou/python /app/python

# 포트 열기
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "/app/app.jar"]