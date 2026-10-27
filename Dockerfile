# =====================================================================
# Stage 1: Build (JDK 21 LTS - Multi-Stage Build)
# =====================================================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# =====================================================================
# Stage 2: Runtime Segura Non-Root (JRE 21 Alpine)
# =====================================================================
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S petcare && adduser -S appuser -G petcare
COPY --from=builder /workspace/build/libs/*.jar app.jar
RUN chown -R appuser:petcare /app

USER appuser

EXPOSE 8080

ENV PORT=8080 \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT}/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -jar /app/app.jar"]
