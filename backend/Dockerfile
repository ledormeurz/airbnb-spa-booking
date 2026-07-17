# Stage 1: Build the application with Maven
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy Maven configuration first for better layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and build
COPY src ./src
RUN mvn package -DskipTests -B -Dmaven.test.skip=true

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the JAR from the builder stage (use fixed name)
COPY --from=builder /build/target/*.jar /app/app.jar

# Verify the JAR exists
RUN test -f /app/app.jar && echo "JAR verified at /app/app.jar" || (echo "ERROR: JAR not found!" && exit 1)

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]