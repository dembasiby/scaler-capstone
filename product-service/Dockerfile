# product-service/Dockerfile
FROM openjdk:17-jdk-slim
VOLUME /tmp
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
COPY target/product-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app.jar"]