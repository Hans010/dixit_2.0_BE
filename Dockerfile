# Multi-stage build: compiles the Quarkus app, then packages it into a slim runtime image.
# Build:  docker build -t dixit-backend:local .
# Run:    docker run -i --rm -p 8080:8080 dixit-backend:local

### Stage 1: build
FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

# Copy Maven wrapper and pom first so dependency resolution is cached across builds
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

# Now copy the rest of the source and build
COPY src src
RUN ./mvnw -B -DskipTests package

### Stage 2: runtime
FROM registry.access.redhat.com/ubi9/openjdk-21:1.23

ENV LANGUAGE='en_US:en'

COPY --from=build --chown=185 /workspace/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185 /workspace/target/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185 /workspace/target/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185 /workspace/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
