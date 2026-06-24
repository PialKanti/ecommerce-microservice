FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace
COPY gradlew ./
COPY gradle ./gradle
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
COPY build.gradle settings.gradle ./
COPY commons/build.gradle ./commons/
COPY commons/src ./commons/src
COPY order-service/build.gradle ./order-service/
COPY order-service/src ./order-service/src
RUN ./gradlew :order-service:bootJar -x test --no-daemon

FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app
COPY --from=builder /workspace/order-service/build/libs/order-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
