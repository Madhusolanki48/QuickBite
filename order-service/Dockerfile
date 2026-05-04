FROM maven:3.9.9-eclipse-temurin-21
WORKDIR /app
COPY .mvn .mvn
COPY src src
COPY pom.xml ./
RUN mvn -q -DskipTests package
EXPOSE 8083
CMD ["java", "-jar", "target/order-service-0.0.1-SNAPSHOT.jar"]
