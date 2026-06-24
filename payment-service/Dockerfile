FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace
COPY gradlew ./
COPY gradle ./gradle
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
COPY build.gradle settings.gradle ./
COPY commons/build.gradle ./commons/
COPY commons/src ./commons/src
COPY payment-service/build.gradle ./payment-service/
COPY payment-service/src ./payment-service/src
RUN ./gradlew :payment-service:bootJar -x test --no-daemon

FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app
COPY --from=builder /workspace/payment-service/build/libs/payment-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
