FROM eclipse-temurin:17-jdk-jammy AS base
LABEL authors="dimitrijeciric04"

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:resolve

COPY src ./src

FROM base AS test
ENTRYPOINT ["./mvnw", "test"]

FROM base AS development
ENTRYPOINT ["./mvnw", "spring-boot:run"]