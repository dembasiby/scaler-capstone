# product-service/Dockerfile
FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/product-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app.jar"]