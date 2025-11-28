# 🏗️ STAGE 1: BUILD
# Use Gradle with JDK 21 on Alpine Linux
FROM gradle:8.5-jdk21-alpine AS builder
WORKDIR /app

# Set dummy environment variables for build time
# (Spring config files reference these, but they're not needed during build)
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/dummy
ENV SPRING_DATASOURCE_USERNAME=dummy
ENV SPRING_DATASOURCE_PASSWORD=dummy

# Copy dependency definitions first (Caching layer)
COPY build.gradle settings.gradle ./
COPY src ./src

# Build the JAR (Skip tests for speed)
RUN gradle bootJar -x test --no-daemon

# 🚀 STAGE 2: RUN
# Use Eclipse Temurin JRE 21 (Lightweight Alpine version)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the JAR from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Set default profile to prod
ENV SPRING_PROFILES_ACTIVE=prod

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]