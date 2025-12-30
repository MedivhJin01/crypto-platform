FROM eclipse-temurin:17-jdk
WORKDIR /app
RUN mkdir -p /app/logs
COPY target/*.jar /app/crypto-app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/crypto-app.jar"]