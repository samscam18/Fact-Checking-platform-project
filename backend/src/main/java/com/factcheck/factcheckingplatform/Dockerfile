# Use a base image with Java 17 (matching your project's JDK version)
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven build artifacts (your JAR file) into the container
# The 'target/*.jar' assumes your Maven build creates a JAR in the 'target' directory.
# Replace 'fact-checking-platform-0.0.1-SNAPSHOT.jar' with your actual JAR file name.
# You can find the exact JAR name in your backend/target/ directory after a successful 'mvn package'.
COPY target/fact-checking-platform-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your Spring Boot application runs on (from application.properties, default 8081)
EXPOSE 8081

# Define the command to run your application when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]

# Optional: Add metadata (useful for documentation)
LABEL maintainer="Samarth Firangi <samarthfirangi44@gmail.com"
LABEL description="Docker image for Fact-Checking Platform Backend"
