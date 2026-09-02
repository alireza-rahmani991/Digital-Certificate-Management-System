# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /build

# Copy wrapper + pom first so dependency resolution is cached separately from source changes
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src ./src
RUN ./mvnw -B clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as non-root
RUN addgroup --system spring && adduser --system --ingroup spring spring
COPY --from=build /build/target/digital_certificate-0.0.1-SNAPSHOT.jar app.jar
RUN chown spring:spring app.jar
USER spring

EXPOSE 8080

# Using the springdoc OpenAPI endpoint since actuator isn't on the classpath.
# Add spring-boot-starter-actuator and switch this to /actuator/health for a proper check.
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -qO- http://localhost:8080/v3/api-docs || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]