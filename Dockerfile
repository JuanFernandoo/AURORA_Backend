# Etapa 1: compilar con Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# Etapa 2: ejecutar solo la API
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/classes ./classes
# La plataforma de hosting inyecta PORT; ApiMain la lee.
EXPOSE 8080
CMD ["java", "-cp", "classes", "com.aurora.api.ApiMain"]
