FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY services/api-gateway/pom.xml services/api-gateway/
RUN ./mvnw dependency:go-offline -pl services/api-gateway -am -q --no-transfer-progress
COPY services/api-gateway/ services/api-gateway/
RUN ./mvnw package -pl services/api-gateway -am -DskipTests --no-transfer-progress -q
RUN java -Djarmode=layertools \
    -jar services/api-gateway/target/api-gateway-*.jar \
    extract --destination /workspace/extracted

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
WORKDIR /app
COPY --from=builder --chown=appuser:appgroup /workspace/extracted/dependencies/          ./
COPY --from=builder --chown=appuser:appgroup /workspace/extracted/spring-boot-loader/    ./
COPY --from=builder --chown=appuser:appgroup /workspace/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /workspace/extracted/application/           ./
EXPOSE 8080
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:docker}", \
    "org.springframework.boot.loader.launch.JarLauncher"]
