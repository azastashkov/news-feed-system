FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY gradle gradle
COPY gradlew gradlew
COPY settings.gradle settings.gradle
COPY build.gradle build.gradle
COPY news-feed-app/build.gradle news-feed-app/build.gradle
COPY news-feed-app/src news-feed-app/src
COPY load-test-client/build.gradle load-test-client/build.gradle

RUN chmod +x gradlew && ./gradlew :news-feed-app:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/news-feed-app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
