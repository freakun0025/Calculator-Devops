# ==========================================
# Stage 1: Build the application
# ==========================================
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Layer caching for dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build the JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Run the application
# ==========================================
# Minimal base image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create and use a non-root user for security
RUN addgroup -S calcgroup && adduser -S calcuser -G calcgroup
USER calcuser

# Copy ONLY the built JAR from the first stage
COPY --from=build /app/target/*.jar app.jar

# Explicitly document the port
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]