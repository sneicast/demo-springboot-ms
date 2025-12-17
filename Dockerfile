FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build
COPY pom.xml .
RUN mvn dependency:resolve
COPY src ./src
RUN mvn clean package -DskipTests -q
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
RUN useradd -m -u 1000 appuser && chown -R appuser:appuser /app
USER appuser
LABEL maintainer="sneicast"
LABEL version="1.0"

EXPOSE 8080

# Ejecutar aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
