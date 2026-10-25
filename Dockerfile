# ==========================================
# Stage 1: Build the application
# ==========================================
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Layer caching for dependencies: only re-downloaded when pom.xml changes
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy source code and build the JAR (tests already run in the Jenkins Test stage)
COPY src ./src
RUN mvn -B clean package -Dmaven.test.skip=true

# ==========================================
# Stage 2: Run the application
# ==========================================
# Minimal base image: JRE only, no JDK/Maven
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create a non-root user for security
RUN addgroup -S calcgroup && adduser -S calcuser -G calcgroup

# Copy ONLY the built JAR from the first stage
COPY --from=build --chown=calcuser:calcgroup /app/target/*.jar app.jar

USER calcuser

# Explicitly document the port
EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=5 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
