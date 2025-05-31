# Stage 1: Build the Java application
FROM maven:3.8.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Build the JAR file
RUN mvn clean package -DskipTests

# Stage 2: Create the final Docker image
FROM eclipse-temurin:17.0.11_9-jre-alpine
WORKDIR /app
# Copy the built JAR from the build stage
COPY --from=build /app/target/time-recording-backend-0.0.1-SNAPSHOT.jar app.jar
# Expose the port your Spring Boot application runs on
EXPOSE 8080
# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]