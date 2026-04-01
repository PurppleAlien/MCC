# Etapa 1: Construcción
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copiar solo archivos de dependencias primero
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar
COPY src ./src
RUN mvn package -DskipTests -B

# Etapa 2: Ejecución
FROM eclipse-temurin:17-jre

WORKDIR /app

# Usuario no root para seguridad
RUN groupadd -g 1001 appgroup && useradd -u 1001 -g appgroup -m appuser
USER appuser

# Copiar el JAR generado desde el stage de build
COPY --from=builder /app/target/*.jar app.jar

# Puerto del microservicio de ordenes
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]