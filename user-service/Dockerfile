# user-service/Dockerfile
FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/user-service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app.jar"]