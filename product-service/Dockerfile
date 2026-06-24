FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace
COPY gradlew ./
COPY gradle ./gradle
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
COPY build.gradle settings.gradle ./
COPY commons/build.gradle ./commons/
COPY commons/src ./commons/src
COPY product-service/build.gradle ./product-service/
COPY product-service/src ./product-service/src
RUN ./gradlew :product-service:bootJar -x test --no-daemon

FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app
COPY --from=builder /workspace/product-service/build/libs/product-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
