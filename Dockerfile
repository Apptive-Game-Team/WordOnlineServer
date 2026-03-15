# Stage 1: Build
FROM gradle:8-jdk21 AS builder

WORKDIR /app

# Copy gradle wrapper and build files first to cache dependencies
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

RUN ./gradlew dependencies --no-daemon

# Copy source and build
COPY src ./src

RUN ./gradlew clean build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
