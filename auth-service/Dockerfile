FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace
COPY gradlew ./
COPY gradle ./gradle
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
COPY build.gradle settings.gradle ./
COPY commons/build.gradle ./commons/
COPY commons/src ./commons/src
COPY auth-service/build.gradle ./auth-service/
COPY auth-service/src ./auth-service/src
RUN ./gradlew :auth-service:bootJar -x test --no-daemon

FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app
COPY --from=builder /workspace/auth-service/build/libs/auth-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
