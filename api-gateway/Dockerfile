# api-gateway/Dockerfile
FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/api-gateway-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]