FROM openjdk:8-jdk-alpine
EXPOSE 8080
ADD /build/libs/chsystem-1.0-SNAPSHOT.jar chsystem-1.0-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "chsystem-1.0-SNAPSHOT.jar"]
