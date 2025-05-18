# config-server/Dockerfile
FROM openjdk:17-jdk-slim
VOLUME /tmp
RUN apt-get update && apt-get install -y curl git && rm -rf /var/lib/apt/lists/*
COPY target/config-server-*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "/app.jar"]