FROM openjdk:8-jdk-alpine

WORKDIR /app

COPY target/agent-c-*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
