FROM ghcr.io/graalvm/graalvm-community:21 AS builder


WORKDIR /opt/app

# Copy Maven wrapper and pom.xml (relative to project root)
COPY .mvn/ .mvn/
COPY mvnw pom.xml lombok.config ./

RUN chmod +x ./mvnw

# Copy source code
COPY src/ ./src/
COPY package.json package-lock.json tsconfig.json type.d.ts vite.config.ts ./
RUN ./mvnw clean install -Pproduction -Pnative -DskipTests


FROM debian:bookworm-slim

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring

ENV LOG_DIR=/opt/app/logs



# Copy jar with fixed name
COPY --from=builder --chown=spring:spring /opt/app/target/remind-pack-vaadin /opt/app/remind-pack-vaadin

RUN mkdir -p "$LOG_DIR" && chown -R spring:spring "$LOG_DIR"
# Switch to non-root user
USER spring

EXPOSE 8080

# Set default values for environment variables
ENV JAVA_OPTS="" \
    JAVA_ARGS=""

# Use exec form with sh to handle environment variables properly
ENTRYPOINT ["sh", "-c", "exec /opt/app/remind-pack-vaadin $JAVA_OPTS $JAVA_ARGS"]