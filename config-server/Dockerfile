# config-server/Dockerfile
FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/config-server-*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "/app.jar"]