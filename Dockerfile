# 1. Use the official Java 21 image
FROM eclipse-temurin:21-jdk-alpine

# 2. Create a working directory inside the container
WORKDIR /app

# 3. Copy the compiled .jar file from your target folder into the container
COPY target/*.jar app.jar

# 4. Expose the standard Spring Boot port
EXPOSE 8080

# 5. The command to run the library application
ENTRYPOINT ["java", "-jar", "app.jar"]