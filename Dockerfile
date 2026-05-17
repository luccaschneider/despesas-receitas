# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN apk add --no-cache wget \
    && addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=build /app/target/despesas-receitas-1.0.0.jar app.jar
# Porta em tempo de execução: defina SERVER_PORT (padrão 8080 na aplicação)
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
