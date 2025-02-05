# Stage 1: Build the application
FROM gradle:8.12.1-jdk17 AS build
WORKDIR /app
COPY ../../Desktop/withyou .
RUN gradle clean build -x test

# Stage 2: Create a lightweight runtime image
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/withyou-0.0.1.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
