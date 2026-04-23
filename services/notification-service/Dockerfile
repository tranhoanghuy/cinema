FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
COPY libs/cinetix-common/pom.xml           libs/cinetix-common/
COPY libs/cinetix-security/pom.xml         libs/cinetix-security/
COPY libs/cinetix-outbox/pom.xml           libs/cinetix-outbox/
COPY libs/cinetix-grpc-stubs/pom.xml       libs/cinetix-grpc-stubs/
COPY services/api-gateway/pom.xml          services/api-gateway/
COPY services/identity-service/pom.xml     services/identity-service/
COPY services/movie-service/pom.xml        services/movie-service/
COPY services/cinema-service/pom.xml       services/cinema-service/
COPY services/showtime-service/pom.xml     services/showtime-service/
COPY services/booking-service/pom.xml      services/booking-service/
COPY services/payment-service/pom.xml      services/payment-service/
COPY services/ticket-service/pom.xml       services/ticket-service/
COPY services/promotion-service/pom.xml    services/promotion-service/
COPY services/notification-service/pom.xml services/notification-service/
COPY services/chat-service/pom.xml         services/chat-service/
RUN ./mvnw dependency:go-offline -pl services/notification-service -am -q --no-transfer-progress
COPY libs/                 libs/
COPY services/notification-service/ services/notification-service/
RUN ./mvnw package -pl services/notification-service -am -DskipTests --no-transfer-progress -q
RUN java -Djarmode=layertools \
    -jar services/notification-service/target/notification-service-*.jar \
    extract --destination /workspace/extracted

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
WORKDIR /app
COPY --from=builder --chown=appuser:appgroup /workspace/extracted/dependencies/          ./
COPY --from=builder --chown=appuser:appgroup /workspace/extracted/spring-boot-loader/    ./
COPY --from=builder --chown=appuser:appgroup /workspace/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /workspace/extracted/application/           ./
EXPOSE 8089
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:docker}", \
    "org.springframework.boot.loader.launch.JarLauncher"]
