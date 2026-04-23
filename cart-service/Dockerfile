FROM maven:3.9.9-eclipse-temurin-21
WORKDIR /app
COPY pom.xml ./
COPY src src
RUN mvn -q -DskipTests package
EXPOSE 8088
CMD ["java", "-jar", "target/cart-service-0.0.1-SNAPSHOT.jar"]