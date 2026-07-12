FROM maven:3.9.11-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S shopwise && adduser -S shopwise -G shopwise
WORKDIR /app
COPY --from=build /workspace/target/app-*.jar app.jar
USER shopwise
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
