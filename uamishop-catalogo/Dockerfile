# Etapa 1: Construcción
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

# Etapa 2: Ejecución
FROM eclipse-temurin:17-jre

WORKDIR /app

RUN groupadd -g 1001 appgroup && useradd -u 1001 -g appgroup -m appuser
USER appuser

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
