# Use OpenJDK as base image
FROM openjdk:17-jdk-slim

# Copy built jar from Maven target folder
COPY target/hello-world-1.0-SNAPSHOT.jar app.jar

# Run the jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
