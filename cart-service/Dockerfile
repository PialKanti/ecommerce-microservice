FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace
COPY gradlew ./
COPY gradle ./gradle
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
COPY build.gradle settings.gradle ./
COPY commons/build.gradle ./commons/
COPY commons/src ./commons/src
COPY cart-service/build.gradle ./cart-service/
COPY cart-service/src ./cart-service/src
RUN ./gradlew :cart-service:bootJar -x test --no-daemon

FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app
COPY --from=builder /workspace/cart-service/build/libs/cart-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
