# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy parent pom and module poms
COPY pom.xml .
COPY promotion-common/pom.xml promotion-common/
COPY promotion-management-service/pom.xml promotion-management-service/

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY promotion-common/src promotion-common/src
COPY promotion-management-service/src promotion-management-service/src

# Build the application
RUN mvn clean package -DskipTests -pl promotion-management-service -am

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/promotion-management-service/target/*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
