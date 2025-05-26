# ==========================================
# Dockerfile
# ==========================================

# Use official Maven image with JDK 17 for building
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Use JRE 17 for runtime
FROM eclipse-temurin:17-jre

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 5051

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]