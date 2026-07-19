# Multi-stage build for Spring Boot
# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and pom.xml first (for better caching)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw package -DskipTests -B

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy built JAR
COPY --from=build /app/target/*.jar app.jar

# Create storage directory
RUN mkdir -p /app/generated-images && chown -R appuser:appgroup /app

USER appuser

# Expose port
EXPOSE 8081

# Environment variables with defaults
ENV SERVER_PORT=8081
ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
