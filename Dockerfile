FROM openjdk:11-jdk-slim as builder

WORKDIR /app

COPY gradle gradle
COPY build.gradle.kts settings.gradle gradlew ./
COPY src src

RUN ./gradlew -Pprofile=prod build

ENTRYPOINT ["sleep", "60000"]

FROM openjdk:8-jre-slim

WORKDIR /app
COPY --from=builder /app/build/libs/chsystem-1.0-SNAPSHOT.jar chsystem-1.0-SNAPSHOT.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "chsystem-1.0-SNAPSHOT.jar"]
