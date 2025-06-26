# Stage 1: Build the JAR using Maven
FROM maven:3.9.3-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run the JAR using a lightweight OpenJDK image
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/Zaikabox-Api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
