FROM maven:3.9.9-eclipse-temurin-21
WORKDIR /app
COPY pom.xml ./
COPY src src
RUN mvn -q -DskipTests package
EXPOSE 8086
CMD ["java", "-jar", "target/review-service-0.0.1-SNAPSHOT.jar"]