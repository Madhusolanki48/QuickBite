FROM maven:3.9.9-eclipse-temurin-21
WORKDIR /app
COPY src src
COPY pom.xml ./
RUN mvn -q -DskipTests package
EXPOSE 8761
CMD ["java", "-jar", "target/eureka-server-0.0.1-SNAPSHOT.jar"]
