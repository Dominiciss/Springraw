FROM maven:3.9.5 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:21-slim
COPY --from=build /target/springraw-0.0.1-SNAPSHOT.jar springraw.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "springraw.jar"]