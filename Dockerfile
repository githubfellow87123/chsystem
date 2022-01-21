FROM eclipse-temurin:17-jdk-alpine as builder

WORKDIR /app

COPY gradle gradle
COPY build.gradle.kts settings.gradle gradlew ./
RUN ./gradlew build 2>/dev/null || true

COPY src src

RUN ./gradlew build

ENTRYPOINT ["sleep", "60000"]

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=builder /app/build/libs/chsystem-1.0-SNAPSHOT.jar chsystem-1.0-SNAPSHOT.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "chsystem-1.0-SNAPSHOT.jar"]
