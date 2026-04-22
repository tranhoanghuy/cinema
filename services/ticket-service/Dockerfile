FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY libs/cinetix-common/pom.xml        libs/cinetix-common/
COPY libs/cinetix-security/pom.xml      libs/cinetix-security/
COPY services/ticket-service/pom.xml    services/ticket-service/
RUN ./mvnw dependency:go-offline -pl services/ticket-service -am -q --no-transfer-progress
COPY libs/            libs/
COPY services/ticket-service/ services/ticket-service/
RUN ./mvnw package -pl services/ticket-service -am -DskipTests --no-transfer-progress -q
RUN java -Djarmode=layertools \
    -jar services/ticket-service/target/ticket-service-*.jar \
    extract --destination /workspace/extracted

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
WORKDIR /app
COPY --from=builder --chown=appuser:appgroup /workspace/extracted/dependencies/          ./
COPY --from=builder --chown=appuser:appgroup /workspace/extracted/spring-boot-loader/    ./
COPY --from=builder --chown=appuser:appgroup /workspace/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /workspace/extracted/application/           ./
EXPOSE 8087
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:docker}", \
    "org.springframework.boot.loader.launch.JarLauncher"]
