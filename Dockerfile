FROM eclipse-temurin:17-jdk

WORKDIR /app
RUN mkdir -p /app/logs

LABEL maintainer="crypto-platform.com"

COPY target/crypto-platform-0.0.1-SNAPSHOT.jar /app/crypto-app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/crypto-app.jar"]