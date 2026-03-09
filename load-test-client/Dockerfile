FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY gradle gradle
COPY gradlew gradlew
COPY settings.gradle settings.gradle
COPY build.gradle build.gradle
COPY load-test-client/build.gradle load-test-client/build.gradle
COPY load-test-client/src load-test-client/src
COPY news-feed-app/build.gradle news-feed-app/build.gradle

RUN chmod +x gradlew && ./gradlew :load-test-client:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/load-test-client/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
