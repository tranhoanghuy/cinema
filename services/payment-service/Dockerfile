FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY libs/cinetix-common/pom.xml       libs/cinetix-common/
COPY libs/cinetix-security/pom.xml     libs/cinetix-security/
COPY libs/cinetix-outbox/pom.xml       libs/cinetix-outbox/
COPY libs/cinetix-grpc-stubs/pom.xml   libs/cinetix-grpc-stubs/
COPY services/payment-service/pom.xml  services/payment-service/
RUN ./mvnw dependency:go-offline -pl services/payment-service -am -q --no-transfer-progress
COPY libs/            libs/
COPY services/payment-service/ services/payment-service/
COPY proto/           proto/
RUN ./mvnw package -pl services/payment-service -am -DskipTests --no-transfer-progress -q
RUN java -Djarmode=layertools \
    -jar services/payment-service/target/payment-service-*.jar \
    extract --destination /workspace/extracted

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
WORKDIR /app
COPY --from=builder --chown=appuser:appgroup /workspace/extracted/dependencies/          ./
COPY --from=builder --chown=appuser:appgroup /workspace/extracted/spring-boot-loader/    ./
COPY --from=builder --chown=appuser:appgroup /workspace/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /workspace/extracted/application/           ./
EXPOSE 8086 9086
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:docker}", \
    "org.springframework.boot.loader.launch.JarLauncher"]
