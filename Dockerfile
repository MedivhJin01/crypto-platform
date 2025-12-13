FROM eclipse-temurin:17-jdk
MAINTAINER crypto-platform.com
COPY target/crypto-platform-0.0.1-SNAPSHOT.jar crypto-app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "crypto-app.jar"]
