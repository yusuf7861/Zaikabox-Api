# Stage 1: Build the JAR using Maven
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
COPY .mvn ./.mvn
COPY mvnw ./
RUN chmod +x mvnw && ./mvnw --no-transfer-progress -DskipTests clean package

# Stage 2: Run the JAR using a lightweight JRE image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/Zaikabox-Api-0.0.1-SNAPSHOT.jar app.jar
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
